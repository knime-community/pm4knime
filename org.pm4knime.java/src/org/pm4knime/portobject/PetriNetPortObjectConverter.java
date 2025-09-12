package org.pm4knime.portobject;

import org.knime.python3.types.port.converter.PortObjectConversionContext;
import org.knime.python3.types.port.converter.PortObjectDecoder;
import org.knime.python3.types.port.converter.PortObjectEncoder;
import org.knime.python3.types.port.converter.PortObjectSpecConversionContext;
import org.knime.python3.types.port.ir.EmptyIntermediateRepresentation;
import org.knime.python3.types.port.ir.JavaEmptyIntermediateRepresentation;
import org.knime.python3.types.port.ir.JavaStringIntermediateRepresentation;
import org.knime.python3.types.port.ir.PortObjectIntermediateRepresentation;
import org.knime.python3.types.port.ir.PortObjectSpecIntermediateRepresentation;
import org.knime.python3.types.port.ir.StringIntermediateRepresentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("restriction")
public class PetriNetPortObjectConverter
    implements PortObjectEncoder<PetriNetPortObject, PetriNetPortObjectSpec>,
    PortObjectDecoder<PetriNetPortObject, StringIntermediateRepresentation, PetriNetPortObjectSpec, EmptyIntermediateRepresentation> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Class<PetriNetPortObject> getPortObjectClass() {
        return PetriNetPortObject.class;
    }

    @Override
    public Class<PetriNetPortObjectSpec> getPortObjectSpecClass() {
        return PetriNetPortObjectSpec.class;
    }

    @Override
    public PetriNetPortObject decodePortObject(final StringIntermediateRepresentation intermediateRepresentation,
        final PetriNetPortObjectSpec spec, final PortObjectConversionContext context) {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(intermediateRepresentation.getStringRepresentation());
            
            Petrinet net = new PetrinetImpl("DecodedPetriNet");
            
            Map<String, Place> placeMap = new HashMap<>();
            Map<String, Transition> transitionMap = new HashMap<>();
            
            Set<Place> initialMarkingPlaces = new HashSet<>();
            Set<Place> finalMarkingPlaces = new HashSet<>();
            
            JsonNode nodesNode = rootNode.get("nodes");
            if (nodesNode != null && nodesNode.isArray()) {
                for (JsonNode nodeJson : nodesNode) {
                    String id = nodeJson.get("id").asText();
                    String type = nodeJson.get("type").asText();
                    String label = nodeJson.has("label") ? nodeJson.get("label").asText() : "";
                    
                    if ("place".equals(type)) {
                        Place place = net.addPlace(id);
                        placeMap.put(id, place);
                        
                        if (nodeJson.has("initial") && nodeJson.get("initial").asBoolean()) {
                            initialMarkingPlaces.add(place);
                        }
                        if (nodeJson.has("final") && nodeJson.get("final").asBoolean()) {
                            finalMarkingPlaces.add(place);
                        }
                    } else if ("transition".equals(type)) {
                        Transition transition = net.addTransition(id);
                        
                        if (label == null || label.isEmpty()) {
                            transition.setInvisible(true);
                        }
                        
                        transitionMap.put(id, transition);
                    }
                }
            }
            
            JsonNode linksNode = rootNode.get("links");
            if (linksNode != null && linksNode.isArray()) {
                for (JsonNode linkJson : linksNode) {
                    String sourceId = linkJson.get("source").asText();
                    String targetId = linkJson.get("target").asText();
                    
                    Object source = placeMap.get(sourceId);
                    if (source == null) {
                        source = transitionMap.get(sourceId);
                    }
                    
                    Object target = placeMap.get(targetId);
                    if (target == null) {
                        target = transitionMap.get(targetId);
                    }
                    
                    if (source instanceof Place && target instanceof Transition) {
                        net.addArc((Place) source, (Transition) target);
                    } else if (source instanceof Transition && target instanceof Place) {
                        net.addArc((Transition) source, (Place) target);
                    }
                }
            }
            
            Marking initialMarking = new Marking();
            for (Place place : initialMarkingPlaces) {
                initialMarking.add(place);
            }
            
            Set<Marking> finalMarkings = new HashSet<>();
            if (!finalMarkingPlaces.isEmpty()) {
                Marking finalMarking = new Marking();
                for (Place place : finalMarkingPlaces) {
                    finalMarking.add(place);
                }
                finalMarkings.add(finalMarking);
            }
            
            AcceptingPetriNet anet = new AcceptingPetriNetImpl(net, initialMarking, finalMarkings);
            
            PetriNetPortObject result = new PetriNetPortObject(anet);
            result.setSpec(spec);
            return result;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode PetriNetPortObject from JSON", e);
        }
    }

    @Override
    public PortObjectIntermediateRepresentation encodePortObject(final PetriNetPortObject portObject,
        final PortObjectConversionContext context) {
        try {
            Map<String, List<?>> jsonMap = portObject.getJSON();
            String jsonString = OBJECT_MAPPER.writeValueAsString(jsonMap);
            return new JavaStringIntermediateRepresentation(jsonString);
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode PetriNetPortObject to JSON string", e);
        }
    }

    @Override
    public PetriNetPortObjectSpec decodePortObjectSpec(
        final EmptyIntermediateRepresentation intermediateRepresentation,
        final PortObjectSpecConversionContext context) {
        return new PetriNetPortObjectSpec();
    }

    @Override
    public PortObjectSpecIntermediateRepresentation encodePortObjectSpec(final PetriNetPortObjectSpec spec,
        final PortObjectSpecConversionContext context) {
        return JavaEmptyIntermediateRepresentation.INSTANCE;
    }
}