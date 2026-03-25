package org.pm4knime.node.conversion.pn2bpmn;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.node.DefaultModel;
import org.pm4knime.node.io.bpmn.writer.BPMNExporter;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.portobject.BpmnPortObjectSpec;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventTrigger;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventUse;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.converters.BPMNUtils;
import org.processmining.plugins.converters.PetriNetToBPMNConverter;
import org.processmining.plugins.graphalgorithms.DFS;

public class PN2BPMNConverterNodeModel {

    private Place initialPlace;
    private Transition initialTransition;

    public PN2BPMNConverterNodeModel(final Class<?> modelSettingsClass) {
    }

    public static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o)
        throws InvalidSettingsException {

        if (!(i.getInPortSpec(0) instanceof PetriNetPortObjectSpec)) {
            throw new InvalidSettingsException("Input is not a valid Petri Net!");
        }

        o.setOutSpec(0, new BpmnPortObjectSpec());
    }

    public static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o) {
        try {
            final PetriNetPortObject pnPO = (PetriNetPortObject)i.getInPortObject(0);
            final AcceptingPetriNet petrinet = pnPO.getANet();

            final PN2BPMNConverterNodeModel model = new PN2BPMNConverterNodeModel(null);
            final BPMNDiagram bpmnDiagram = model.convert(petrinet);
            final String modelXml = BPMNExporter.convertToXML(bpmnDiagram);
            final BpmnPortObject bpmnPO = new BpmnPortObject(modelXml);

            o.setOutData(0, bpmnPO);
            o.setInternalData(bpmnPO);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public BPMNDiagram convert(final AcceptingPetriNet petrinet) {

        final Marking initialMarking = petrinet.getInitialMarking();
        final Set<Marking> finalMarking = petrinet.getFinalMarkings();

        final Object[] cloneResult = cloneToPetrinet(petrinet.getNet(), initialMarking, finalMarking);
        final PetrinetGraph clonePetrinet = (PetrinetGraph)cloneResult[0];
        final Marking cloneInitialMarking = (Marking)cloneResult[3];
        final Marking cloneFinalMarking = (Marking)cloneResult[4];

        final Map<PetrinetNode, Set<PetrinetNode>> deletedResetArcs = deleteResetArcs(clonePetrinet);
        restoreResetArcs(deletedResetArcs, clonePetrinet);

        convertToResemblingFreeChoice(clonePetrinet);
        convertToPetrinetWithOneSourcePlace(clonePetrinet, cloneInitialMarking);
        handleTransitionsWithoutIncomingFlows(clonePetrinet);
        removeDeadPlaces(clonePetrinet);

        final PetriNetToBPMNConverter converter = new PetriNetToBPMNConverter(clonePetrinet, initialPlace,
            cloneFinalMarking);
        final BPMNDiagram bpmnDiagram = converter.convert();
        final Map<String, Activity> transitionConversionMap = converter.getTransitionConversionMap();
        BPMNUtils.simplifyBPMNDiagram(transitionConversionMap, bpmnDiagram);

        if (cloneFinalMarking != null && cloneFinalMarking.size() > 0) {
            handleActivitiesWithoutOutgoingFlows(bpmnDiagram);
        }

        return bpmnDiagram;
    }

    @SuppressWarnings("rawtypes")
    private void handleTransitionsWithoutIncomingFlows(final PetrinetGraph petriNet) {
        for (Transition transition : petriNet.getTransitions()) {
            if (petriNet.getInEdges(transition) == null || petriNet.getInEdges(transition).size() == 0) {
                Place newPlace = petriNet.addPlace("");
                petriNet.addArc(initialTransition, newPlace);
                petriNet.addArc(newPlace, transition);
                petriNet.addArc(transition, newPlace);
            }
        }
    }

    private void convertToPetrinetWithOneSourcePlace(final PetrinetGraph petriNet, final Marking marking) {
        initialPlace = petriNet.addPlace("");
        initialTransition = petriNet.addTransition("");
        initialTransition.setInvisible(true);
        petriNet.addArc(initialPlace, initialTransition);
        for (Place place : marking.toList()) {
            petriNet.addArc(initialTransition, place);
        }
    }

    private void handleActivitiesWithoutOutgoingFlows(final BPMNDiagram bpmnDiagram) {
        Event startEvent = retrieveStartEvent(bpmnDiagram);
        Event endEvent = retrieveEndEvent(bpmnDiagram);
        DFS dfs = new DFS(bpmnDiagram, startEvent);

        Set<Activity> activitiesWithoutPathToEndEvent = findActivitiesWithoutPathToEndEvent(bpmnDiagram, dfs);
        Set<Activity> currentActivities = new HashSet<>();
        currentActivities.addAll(activitiesWithoutPathToEndEvent);

        for (Activity activity1 : activitiesWithoutPathToEndEvent) {
            for (Activity activity2 : activitiesWithoutPathToEndEvent) {
                if (dfs.findDescendants(activity2).contains(activity1) && !activity1.equals(activity2)) {
                    if (currentActivities.contains(activity1)) {
                        currentActivities.remove(activity2);
                    }
                }
            }
        }

        for (Activity activity : currentActivities) {
            bpmnDiagram.addFlow(activity, endEvent, "");
        }
    }

    private Set<Activity> findActivitiesWithoutPathToEndEvent(final BPMNDiagram bpmnDiagram, final DFS dfs) {

        Set<Activity> resultSet = new HashSet<>();
        Event endEvent = retrieveEndEvent(bpmnDiagram);
        for (Activity activity : bpmnDiagram.getActivities()) {
            Set<DirectedGraphNode> descendants = dfs.findDescendants(activity);

            boolean hasPathToEndEvent = false;
            for (DirectedGraphNode descendant : descendants) {
                if (descendant.equals(endEvent)) {
                    hasPathToEndEvent = true;
                }
            }
            if (!hasPathToEndEvent) {
                resultSet.add(activity);
            }
        }
        return resultSet;
    }

    private void removeDeadPlaces(final PetrinetGraph petriNet) {
        boolean hasDeadPlaces;
        Set<Place> toRemove = new HashSet<>();
        do {
            hasDeadPlaces = false;
            for (Place place : petriNet.getPlaces()) {
                if (place != initialPlace) {
                    if (petriNet.getInEdges(place) == null || petriNet.getInEdges(place).size() == 0) {
                        Collection<Transition> outTransitions = collectOutTransitions(place, petriNet);
                        for (Transition transition : outTransitions) {
                            petriNet.removeTransition(transition);
                        }
                        toRemove.add(place);
                        hasDeadPlaces = true;
                    }
                }
            }
            for (Place place : toRemove) {
                petriNet.removePlace(place);
            }
        } while (hasDeadPlaces);
    }

    private void convertToResemblingFreeChoice(final PetrinetGraph petrinetGraph) {
        Set<Place> nonFreePlaces = new HashSet<>();
        for (Transition t1 : petrinetGraph.getTransitions()) {
            for (Transition t2 : petrinetGraph.getTransitions()) {
                Set<Place> inPlaces1 = collectInPlaces(t1, petrinetGraph);
                Set<Place> inPlaces2 = collectInPlaces(t2, petrinetGraph);
                Set<Place> commonPlaces = new HashSet<>();
                boolean hasCommonPlace = false;
                boolean hasDiffPlaces = false;
                for (Place p1 : inPlaces1) {
                    for (Place p2 : inPlaces2) {
                        if (p1.equals(p2)) {
                            hasCommonPlace = true;
                            commonPlaces.add(p1);
                        } else {
                            hasDiffPlaces = true;
                        }
                    }
                }
                if (hasCommonPlace && hasDiffPlaces) {
                    nonFreePlaces.addAll(commonPlaces);
                }
            }
        }
        splitNonFreePlaces(petrinetGraph, nonFreePlaces);
    }

    private void splitNonFreePlaces(final PetrinetGraph petrinetGraph, final Set<Place> nonFreePlaces) {
        for (Place place : nonFreePlaces) {
            for (PetrinetEdge<?, ?> outArc : petrinetGraph.getOutEdges(place)) {
                Transition outTransition = (Transition)outArc.getTarget();
                petrinetGraph.removeEdge(outArc);
                Place newPlace = petrinetGraph.addPlace("");
                Transition newTransition = petrinetGraph.addTransition("");
                newTransition.setInvisible(true);
                petrinetGraph.addArc(newPlace, outTransition);
                petrinetGraph.addArc(newTransition, newPlace);
                petrinetGraph.addArc(place, newTransition);
            }
        }
    }

    private Map<PetrinetNode, Set<PetrinetNode>> deleteResetArcs(final PetrinetGraph petrinetGraph) {

        Map<PetrinetNode, Set<PetrinetNode>> deletedEdges = new HashMap<>();
        for (Place place : petrinetGraph.getPlaces()) {
            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges =
                petrinetGraph.getOutEdges(place);
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : outEdges) {
                if (edge instanceof ResetArc) {
                    petrinetGraph.removeEdge(edge);
                    Set<PetrinetNode> targetNodes;
                    if (deletedEdges.get(edge) == null) {
                        targetNodes = new HashSet<>();
                        deletedEdges.put(place, targetNodes);
                    } else {
                        targetNodes = deletedEdges.get(edge);
                    }
                    targetNodes.add(edge.getTarget());
                    deletedEdges.put(place, targetNodes);
                }
            }
        }

        return deletedEdges;
    }

    private void restoreResetArcs(final Map<PetrinetNode, Set<PetrinetNode>> resetArcs,
        final PetrinetGraph petrinetGraph) {

        for (PetrinetNode place : resetArcs.keySet()) {
            Set<PetrinetNode> transitions = resetArcs.get(place);
            for (PetrinetNode transition : transitions) {
                if (petrinetGraph instanceof ResetInhibitorNet) {
                    ((ResetInhibitorNet)petrinetGraph).addResetArc((Place)place, (Transition)transition);
                }
                if (petrinetGraph instanceof ResetNet) {
                    ((ResetNet)petrinetGraph).addResetArc((Place)place, (Transition)transition);
                }
            }
        }
    }

    private Event retrieveEndEvent(final BPMNDiagram diagram) {
        Event endEvent = null;
        for (Event event : diagram.getEvents()) {
            if (event.getEventType().equals(EventType.END)) {
                endEvent = event;
            }
        }

        if (endEvent == null) {
            endEvent = diagram.addEvent("", EventType.END, EventTrigger.NONE, EventUse.THROW, true, null);
        }
        return endEvent;
    }

    private Event retrieveStartEvent(final BPMNDiagram diagram) {
        Event startEvent = null;
        for (Event event : diagram.getEvents()) {
            if (event.getEventType().equals(EventType.START)) {
                startEvent = event;
            }
        }

        return startEvent;
    }

    private Set<Transition> collectOutTransitions(final Place place, final PetrinetGraph petrinetGraph) {
        Set<Transition> outTransitions = new HashSet<>();
        Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges =
            petrinetGraph.getOutEdges(place);
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> outEdge : outEdges) {
            if (!(outEdge instanceof ResetArc)) {
                outTransitions.add((Transition)outEdge.getTarget());
            }
        }
        return outTransitions;
    }

    private Set<Place> collectInPlaces(final Transition transition, final PetrinetGraph petrinetGraph) {
        Set<Place> inPlaces = new HashSet<>();
        Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges =
            petrinetGraph.getInEdges(transition);
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> inEdge : inEdges) {
            inPlaces.add((Place)inEdge.getSource());
        }
        return inPlaces;
    }

    private Object[] cloneToPetrinet(final PetrinetGraph petriNet, final Marking initialMarking,
        final Set<Marking> finalMarkings) {

        ResetInhibitorNet clonePetriNet = new ResetInhibitorNetImpl(petriNet.getLabel());
        Map<Transition, Transition> transitionsMap = new HashMap<>();
        Map<Place, Place> placesMap = new HashMap<>();
        Marking newInitialMarking = new Marking();
        Marking newFinalMarking = new Marking();

        for (Transition transition : petriNet.getTransitions()) {
            Transition newTransition = clonePetriNet.addTransition(transition.getLabel());
            newTransition.setInvisible(transition.isInvisible());
            transitionsMap.put(transition, newTransition);
        }
        for (Place place : petriNet.getPlaces()) {
            placesMap.put(place, clonePetriNet.addPlace(place.getLabel()));
        }
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : petriNet.getEdges()) {
            if (edge instanceof InhibitorArc) {
                if (edge.getSource() instanceof Place && edge.getTarget() instanceof Transition) {
                    clonePetriNet.addInhibitorArc(placesMap.get(edge.getSource()),
                        transitionsMap.get(edge.getTarget()));
                }
            }
            if (edge instanceof ResetArc) {
                if (edge.getSource() instanceof Place && edge.getTarget() instanceof Transition) {
                    clonePetriNet.addResetArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
                }
            }
            if (edge instanceof Arc) {
                if (edge.getSource() instanceof Place && edge.getTarget() instanceof Transition) {
                    clonePetriNet.addArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
                }
                if (edge.getSource() instanceof Transition && edge.getTarget() instanceof Place) {
                    clonePetriNet.addArc(transitionsMap.get(edge.getSource()), placesMap.get(edge.getTarget()));
                }
            }
        }

        if (initialMarking != null) {
            for (Place markedPlace : initialMarking.toList()) {
                newInitialMarking.add(placesMap.get(markedPlace));
            }
        }

        if (finalMarkings.size() == 1) {
            Marking fm = finalMarkings.iterator().next();
            if (fm != null) {
                for (Place markedPlace : fm.toList()) {
                    newFinalMarking.add(placesMap.get(markedPlace));
                }
            }
        }

        return new Object[]{clonePetriNet, transitionsMap, placesMap, newInitialMarking, newFinalMarking};
    }
}
