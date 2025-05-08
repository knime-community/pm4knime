package org.pm4knime.node.conversion.pn2bpmn;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.portobject.BpmnPortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
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
import org.pm4knime.node.io.bpmn.writer.BPMNExporter;

/**
 * <code>NodeModel</code> for the "PN2BPMNConverter" node. It converts a process
 * tree into Petri net. Since the conversion is guaranteed to work, so no need
 * of NodeDialog.
 *
 * @author Sanjida Islam Ivy
 */

@SuppressWarnings("restriction")
public class PN2BPMNConverterNodeModel extends
		AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {
	protected AbstractJSONPortObject bpmnPO; 
	protected PetriNetPortObject pnPO;
	private Place initialPlace;
	private Transition initialTransition;

	protected EmptyNodeSettings m_settings = new EmptyNodeSettings();

	private final Class<EmptyNodeSettings> m_settingsClass;


	public PN2BPMNConverterNodeModel(Class<EmptyNodeSettings> modelSettingsClass) {
		// TODO Auto-generated constructor stub
		super(new PortType[] { PetriNetPortObject.TYPE }, new PortType[] { BpmnPortObject.TYPE }, "BPMN JS View");
		m_settingsClass = modelSettingsClass;
	}

	@Override
	protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
			final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		return new PortObject[] { bpmnPO };
	}

	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		pnPO = (PetriNetPortObject) inObjects[0]; // Get the Petrinet PortObject from input
		AcceptingPetriNet petrinet = pnPO.getANet();// Get the Petrinet from the PortObject

		BPMNDiagram bpmnDiagram = convert(petrinet);

		String model_xml = BPMNExporter.convertToXML(bpmnDiagram);
		bpmnPO = new BpmnPortObject(model_xml);
		JSGraphVizViewRepresentation representation = getViewRepresentation(); // Get the view representation

		representation.setJSONString(bpmnPO.getJSON());
	}

	public BPMNDiagram convert(AcceptingPetriNet petrinet) {

		Marking initialMarking = petrinet.getInitialMarking();

		Set<Marking> finalMarking = petrinet.getFinalMarkings();

		// Clone to Petri net with marking
		Object[] cloneResult = cloneToPetrinet(petrinet.getNet(), initialMarking, finalMarking);
		PetrinetGraph clonePetrinet = (PetrinetGraph) cloneResult[0];
		Map<Transition, Transition> transitionsMap = (Map<Transition, Transition>) cloneResult[1];
		Map<Place, Place> placesMap = (Map<Place, Place>) cloneResult[2];
		Marking cloneInitialMarking = (Marking) cloneResult[3];
		Marking cloneFinalMarking = (Marking) cloneResult[4];

		// Check whether Petri net without reset arcs is a free-choice net
		Map<PetrinetNode, Set<PetrinetNode>> deletedResetArcs = deleteResetArcs(clonePetrinet);
//		boolean isFreeChoice = petriNetIsFreeChoice(context, clonePetrinet);
		restoreResetArcs(deletedResetArcs, clonePetrinet);

		convertToResemblingFreeChoice(clonePetrinet);

		// Convert to a Petri net with one source place if needed
		convertToPetrinetWithOneSourcePlace(clonePetrinet, cloneInitialMarking);

		// Handle transitions without incoming sequence flows
		handleTransitionsWithoutIncomingFlows(clonePetrinet);

		// Remove places without incoming sequence flows
		removeDeadPlaces(clonePetrinet);

		// Convert Petri net to a BPMN diagram
		PetriNetToBPMNConverter converter = new PetriNetToBPMNConverter(clonePetrinet, initialPlace, cloneFinalMarking);
		BPMNDiagram bpmnDiagram = converter.convert();
		Map<String, Activity> transitionConversionMap = converter.getTransitionConversionMap();
		Map<Place, Flow> placeConversionMap = converter.getPlaceConversionMap();

		// Simplify BPMN diagram
		BPMNUtils.simplifyBPMNDiagram(transitionConversionMap, bpmnDiagram);

		// Handle activities without outgoing sequence flows
		if ((cloneFinalMarking != null) && (cloneFinalMarking.size() > 0)) {
			handleActivitiesWithoutOutgoingFlows(bpmnDiagram);
		}

		System.out.println("bpmnDiagram");
		System.out.println(bpmnDiagram.getActivities());
		return bpmnDiagram;
	}

	@SuppressWarnings("rawtypes")
	private void handleTransitionsWithoutIncomingFlows(PetrinetGraph petriNet) {

		// Handle transitions without incoming edges
		for (Transition transition : petriNet.getTransitions()) {
			if ((petriNet.getInEdges(transition) == null) || (petriNet.getInEdges(transition).size() == 0)) {
				Place newPlace = petriNet.addPlace("");
				petriNet.addArc(initialTransition, newPlace);
				petriNet.addArc(newPlace, transition);
				petriNet.addArc(transition, newPlace);
			}
		}
	}

	private void convertToPetrinetWithOneSourcePlace(PetrinetGraph petriNet, Marking marking) {
		initialPlace = petriNet.addPlace("");
		initialTransition = petriNet.addTransition("");
		initialTransition.setInvisible(true);
		petriNet.addArc(initialPlace, initialTransition);
		for (Place place : marking.toList()) {
			petriNet.addArc(initialTransition, place);
		}
	}

	private void handleActivitiesWithoutOutgoingFlows(BPMNDiagram bpmnDiagram) {
		// Handle activities without paths to the end event
		Event startEvent = retrieveStartEvent(bpmnDiagram);
		Event endEvent = retrieveEndEvent(bpmnDiagram);
		DFS dfs = new DFS(bpmnDiagram, startEvent);

		Set<Activity> acivitiesWithoutPathToEndEvent = findActivitiesWithoutPathToEndEvent(bpmnDiagram, dfs);
		Set<Activity> currentActivities = new HashSet<Activity>();
		currentActivities.addAll(acivitiesWithoutPathToEndEvent);

		for (Activity activity1 : acivitiesWithoutPathToEndEvent) {
			for (Activity activity2 : acivitiesWithoutPathToEndEvent) {
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

	private Set<Activity> findActivitiesWithoutPathToEndEvent(BPMNDiagram bpmnDiagram, DFS dfs) {

		Set<Activity> resultSet = new HashSet<Activity>();
		Event endEvent = retrieveEndEvent(bpmnDiagram);
		// Find activities without paths to end event
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

	/**
	 * Remove places without incoming sequence flows and corresponding output
	 * transitions
	 * 
	 * @param petriNet
	 */
	private void removeDeadPlaces(PetrinetGraph petriNet) {
		boolean hasDeadPlaces;
		Set<Place> toRemove = new HashSet<Place>();
		do {
			hasDeadPlaces = false;
			for (Place place : petriNet.getPlaces()) {
				if (place != initialPlace) {
					if ((petriNet.getInEdges(place) == null) || (petriNet.getInEdges(place).size() == 0)) {
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


	private Place retrieveSourcePlace(PetrinetGraph petrinetGraph) {
		for (Place place : petrinetGraph.getPlaces()) {
			if ((petrinetGraph.getInEdges(place) == null) || (petrinetGraph.getInEdges(place).size() == 0)) {
				return place;
			}
		}
		return null;
	}

	/**
	 * Convert to resembling free-choice net
	 * 
	 * @param petrinetGraph
	 */
	private void convertToResemblingFreeChoice(PetrinetGraph petrinetGraph) {
		// Set of common places which should be splited
		Set<Place> nonFreePlaces = new HashSet<Place>();
		// For each pair of transitions
		for (Transition t1 : petrinetGraph.getTransitions()) {
			for (Transition t2 : petrinetGraph.getTransitions()) {
				Set<Place> inPlaces1 = collectInPlaces(t1, petrinetGraph);
				Set<Place> inPlaces2 = collectInPlaces(t2, petrinetGraph);
				Set<Place> commonPlaces = new HashSet<Place>();
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

	/**
	 * Split non-free places
	 * 
	 * @param petrinetGraph
	 * @param nonFreePlaces
	 */
	private void splitNonFreePlaces(PetrinetGraph petrinetGraph, Set<Place> nonFreePlaces) {
		for (Place place : nonFreePlaces) {
			for (PetrinetEdge<?, ?> outArc : petrinetGraph.getOutEdges(place)) {
				Transition outTransition = (Transition) outArc.getTarget();
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

	
	/**
	 * Delete reset arcs
	 * 
	 * @param petrinetGraph
	 * @return
	 */
	private Map<PetrinetNode, Set<PetrinetNode>> deleteResetArcs(PetrinetGraph petrinetGraph) {

		Map<PetrinetNode, Set<PetrinetNode>> deletedEdges = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		for (Place place : petrinetGraph.getPlaces()) {
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = petrinetGraph
					.getOutEdges(place);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : outEdges) {
				if (edge instanceof ResetArc) {
					petrinetGraph.removeEdge(edge);
					Set<PetrinetNode> targetNodes;
					if (deletedEdges.get(edge) == null) {
						targetNodes = new HashSet<PetrinetNode>();
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

	/**
	 * Restore reset arcs
	 * 
	 * @param petrinetGraph
	 * @return
	 */
	private void restoreResetArcs(Map<PetrinetNode, Set<PetrinetNode>> resetArcs, PetrinetGraph petrinetGraph) {

		for (PetrinetNode place : resetArcs.keySet()) {
			Set<PetrinetNode> transitions = resetArcs.get(place);
			for (PetrinetNode transition : transitions) {
				if (petrinetGraph instanceof ResetInhibitorNet) {
					((ResetInhibitorNet) petrinetGraph).addResetArc((Place) place, (Transition) transition);
				}
				if (petrinetGraph instanceof ResetNet) {
					System.out.println("Reset arc is added");
					((ResetNet) petrinetGraph).addResetArc((Place) place, (Transition) transition);
				}
			}
		}
	}

	/**
	 * Retrieve end event for the BPMN Diagram
	 * 
	 * @param diagram
	 * @return
	 */
	private Event retrieveEndEvent(BPMNDiagram diagram) {
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

	/**
	 * Retrieve start event for the BPMN Diagram
	 * 
	 * @param diagram
	 * @return
	 */
	private Event retrieveStartEvent(BPMNDiagram diagram) {
		Event startEvent = null;
		for (Event event : diagram.getEvents()) {
			if (event.getEventType().equals(EventType.START)) {
				startEvent = event;
			}
		}

		return startEvent;
	}

	/**
	 * Collect out transitions for a place in the Petri net
	 * 
	 * @param place
	 * @param petrinetGraph
	 * @return
	 */
	private Set<Transition> collectOutTransitions(Place place, PetrinetGraph petrinetGraph) {
		Set<Transition> outTransitions = new HashSet<Transition>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = petrinetGraph
				.getOutEdges(place);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> outEdge : outEdges) {
			if (!(outEdge instanceof ResetArc)) {
				outTransitions.add((Transition) outEdge.getTarget());
			}
		}
		return outTransitions;
	}

	/**
	 * Collect in places for a transition in the Petri net
	 * 
	 * @param transition
	 * @param petrinetGraph
	 * @return
	 */
	private Set<Place> collectInPlaces(Transition transition, PetrinetGraph petrinetGraph) {
		Set<Place> inPlaces = new HashSet<Place>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = petrinetGraph
				.getInEdges(transition);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> inEdge : inEdges) {
			inPlaces.add((Place) inEdge.getSource());
		}
		return inPlaces;
	}

	/**
	 * Clone Petri net
	 * 
	 * @param dataPetriNet
	 * @return
	 */
	private Object[] cloneToPetrinet(PetrinetGraph petriNet, Marking initialMarking, Set<Marking> finalMarkings) {
		ResetInhibitorNet clonePetriNet = new ResetInhibitorNetImpl(petriNet.getLabel());
		Map<Transition, Transition> transitionsMap = new HashMap<Transition, Transition>();
		Map<Place, Place> placesMap = new HashMap<Place, Place>();
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
				if ((edge.getSource() instanceof Place) && (edge.getTarget() instanceof Transition)) {
					clonePetriNet.addInhibitorArc(placesMap.get(edge.getSource()),
							transitionsMap.get(edge.getTarget()));
				}
			}
			if (edge instanceof ResetArc) {
				if ((edge.getSource() instanceof Place) && (edge.getTarget() instanceof Transition)) {
					clonePetriNet.addResetArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
				}
			}
			if (edge instanceof Arc) {
				if ((edge.getSource() instanceof Place) && (edge.getTarget() instanceof Transition)) {
					clonePetriNet.addArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
				}
				if ((edge.getSource() instanceof Transition) && (edge.getTarget() instanceof Place)) {
					clonePetriNet.addArc(transitionsMap.get(edge.getSource()), placesMap.get(edge.getTarget()));
				}
			}
		}

		// Construct marking for the clone Petri net
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

		return new Object[] { clonePetriNet, transitionsMap, placesMap, newInitialMarking, newFinalMarking };
	}

	// Configure the node based on input port object specifications
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		// Check if the input is a valid PetriNetPortObjectSpec
		if (m_settings == null) {
			m_settings = DefaultNodeSettings.createSettings(m_settingsClass, inSpecs);
		}

		PetriNetPortObjectSpec spec = (PetriNetPortObjectSpec) inSpecs[0];

		if (!spec.getClass().equals(PetriNetPortObjectSpec.class)) {
			throw new InvalidSettingsException("Input is not a valid Petri Net!");
		}

		PetriNetPortObjectSpec pnSpec = (PetriNetPortObjectSpec) inSpecs[0];

		return new PortObjectSpec[] { pnSpec };
	}

	// Configure the output PortObject specification
	protected PortObjectSpec[] configureOutSpec(BpmnPortObjectSpec logSpec) {
		return new PortObjectSpec[] { new BpmnPortObjectSpec() };
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// TODO: generated method stub
		if (m_settings != null) {
			DefaultNodeSettings.saveSettings(m_settingsClass, m_settings, settings);
		}
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_settings = DefaultNodeSettings.loadSettings(settings, m_settingsClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
	}

	@Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	@Override
	protected boolean generateImage() {
		return false;
	}

	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
	}

	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}

	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
	}

	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] { pnPO };
	}

	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		pnPO = (PetriNetPortObject) portObjects[0];

	}

}