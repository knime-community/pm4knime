package org.pm4knime.portobject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.util.PetriNetUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;

import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;


public class PetriNetPortObject extends AbstractJSONPortObject {

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class);
	public static final PortType TYPE_OPTIONAL =
			PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class, true);
	
	private static final String ZIP_ENTRY_NAME = "PetriNetPortObject";
	
	public static final String PETRI_NET_TEXT = "A Petri net is a directed bipartite graph used to model processes. It consists of places, transitions, and directed arcs connecting them. "
			+ "A place is enabled if it it contains at least one token. A transition can only fire if all incoming places are enabled. After firing a transition, "
			+ "a token is consumed from all of its incoming places, and a token is produced in all of its outgoing places. The initial marking indicates the initial state of the Petri net. "
			+ "Places that belong to the initial marking are marked by green tokens inside them. The final marking denotes the final state of the Petri net. "
			+ "Places within the final marking are highlighted with a heavier border.";
	
	
	// use AcceptingPetriNet as the model
	// m_anet: a field that carries anet
	AcceptingPetriNet m_anet ;
	PetriNetPortObjectSpec m_spec;
	public PetriNetPortObject() {}
	
	public PetriNetPortObject(AcceptingPetriNet anet) {
		m_anet = anet;
	}

	
	public AcceptingPetriNet getANet() {
		return m_anet;
	}

	public void setANet(AcceptingPetriNet anet) {
		m_anet = anet;
	}

	@Override
	public String getSummary() {
		return "Transitions: " + m_anet.getNet().getTransitions().size() + ", Places: " + m_anet.getNet().getPlaces().size();
	}

	public boolean equals(Object o) {
		return m_anet.equals(o);
	}
	
	
	@Override
	public PetriNetPortObjectSpec getSpec() {
		if(m_spec!=null) {
			m_spec.setTransitions(m_anet.getNet().getTransitions());
			return m_spec;
		}
		PetriNetPortObjectSpec spec = new PetriNetPortObjectSpec();
		spec.setTransitions(m_anet.getNet().getTransitions());
		return spec;
	}

	public void setSpec(PortObjectSpec spec) {
		m_spec = (PetriNetPortObjectSpec) spec;
	}

	@Override
	public JComponent[] getViews() {
		return new JComponent[] {};
	}

	
	// here we serialise the PortObject by using the prom plugin
	public static class PetriNetPortObjectSerializer extends PortObjectSerializer<PetriNetPortObject> {

		@Override
		public void savePortObject(PetriNetPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
				throws IOException, CanceledExecutionException {
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			
			// do we need the layout to import or export the AccepingPetrinet??
			// we can't use the ObjectOutputStream, because AcceptingPetrinet not serialized..
			// recode it from AcceptingPetriNetImpl.exportToFile()
			PetriNetUtil.exportToStream(portObject.getANet(), out);
			out.closeEntry();
			// out.close();
			
		}

		@Override
		public PetriNetPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			// nothing to do with spec here 
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			
			PetriNetPortObject result = null;
			try {
				// they put layout information into context, if we want to show the them, 
				// we need to keep the context the same in load and save program. But how to do this??
				// that's why there is context in portObject. If we also save the context, what can be done??
				AcceptingPetriNet anet = PetriNetUtil.importFromStream(in);
				result = new PetriNetPortObject(anet);
				result.setSpec(spec);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// in.close();
			
			return result;
		}
		
	}


	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}
		
	public Map<String, List<?>> getJSON() {
	    Map<String, List<?>> result = new HashMap<>();
	    
	    Set<Place> finalMarkingPlaces = new TreeSet<Place>();
	    for (Marking setMarkings : m_anet.getFinalMarkings())
	        finalMarkingPlaces.addAll(setMarkings);	
	    
	    List<Map<String, Object>> nodes = new ArrayList<>();
	    
	    for(Place place : m_anet.getNet().getPlaces()) {
	        Map<String, Object> placeNode = new HashMap<>();
	        placeNode.put("id", place.getId().toString());
	        placeNode.put("type", "place");
	        placeNode.put("label", "");
	        placeNode.put("initial", m_anet.getInitialMarking().contains(place));
	        placeNode.put("final", finalMarkingPlaces.contains(place));
	        nodes.add(placeNode);
	    }
	    
	    for (Transition transition : m_anet.getNet().getTransitions()) {
	        Map<String, Object> transitionNode = new HashMap<>();
	        transitionNode.put("id", transition.getId().toString());
	        transitionNode.put("type", "transition");
	        
	        String label = transition.getLabel();
	        if (transition.isInvisible()) {
	            transitionNode.put("label", "");
	        } else {
	            transitionNode.put("label", label != null ? label : "");
	        }
	        
	        nodes.add(transitionNode);
	    }
	    
	    result.put("nodes", nodes);
	    
	    List<Map<String, Object>> links = new ArrayList<>();
	    
	    for (DirectedGraphEdge<?, ?> edge : m_anet.getNet().getEdges()) {
	        Map<String, Object> link = new HashMap<>();
	        link.put("source", edge.getSource().getId().toString());
	        link.put("target", edge.getTarget().getId().toString());
	        links.add(link);
	    }

	    result.put("links", links);
	    
//	    System.out.println(result);
	    return result;
	}
	
	
}
