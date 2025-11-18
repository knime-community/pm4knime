package org.pm4knime.portobject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.processmining.models.graphbased.NodeID;
import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraph;
import org.processmining.plugins.inductiveminer2.plugins.DfgMsdImportPlugin;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
	

public class DfgMsdPortObject extends AbstractJSONPortObject {

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(DfgMsdPortObject.class);
	private static final String ZIP_ENTRY_NAME = "DfgMsdPortObject";
	private static final int BUFFER_SIZE = 8192 * 4;
	private static final String CHARSET = Charset.defaultCharset().name();
	DfgMsd dfm;
	DfgMsdPortObjectSpec m_spec;
	public DfgMsdPortObject() {
	}

	public DfgMsdPortObject(DfgMsd dfm) {
		this.dfm = dfm;
	}

	public DfgMsd getDfgMsd() {
		return dfm;
	}

	public void setDfgMsd(DfgMsd dfm) {
		this.dfm = dfm;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return "Nodes: " + dfm.getDirectlyFollowsGraph().getNumberOfNodes();
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;

	    DfgMsdPortObject other = (DfgMsdPortObject) obj;

	    DfgMsd dfm1 = this.dfm;
	    DfgMsd dfm2 = other.dfm;

	    if (dfm1 == null && dfm2 == null) return true;
	    if (dfm1 == null || dfm2 == null) return false;

	    List<String> activities1 = getNormalizedActivities(dfm1);
	    List<String> activities2 = getNormalizedActivities(dfm2);
	    if (!activities1.equals(activities2)) return false;

	    Map<String, Long> startActivities1 = getNormalizedStartActivities(dfm1);
	    Map<String, Long> startActivities2 = getNormalizedStartActivities(dfm2);
	    if (!startActivities1.equals(startActivities2)) return false;

	    Map<String, Long> endActivities1 = getNormalizedEndActivities(dfm1);
	    Map<String, Long> endActivities2 = getNormalizedEndActivities(dfm2);
	    if (!endActivities1.equals(endActivities2)) return false;

	    List<String> dfgEdges1 = getNormalizedDfgEdges(dfm1);
	    List<String> dfgEdges2 = getNormalizedDfgEdges(dfm2);
	    return dfgEdges1.equals(dfgEdges2);
	}

	@Override
	public int hashCode() {
	    if (dfm == null) return 0;

	    int result = 17;
	    result = 31 * result + getNormalizedActivities(dfm).hashCode();
	    result = 31 * result + getNormalizedStartActivities(dfm).hashCode();
	    result = 31 * result + getNormalizedEndActivities(dfm).hashCode();
	    result = 31 * result + getNormalizedDfgEdges(dfm).hashCode();
	    return result;
	}

	private List<String> getNormalizedActivities(DfgMsd dfm) {
	    List<String> activities = new ArrayList<>();
	    for (String activity : dfm.getAllActivities()) {
	        activities.add(activity);
	    }
	    Collections.sort(activities);
	    return activities;
	}

	private Map<String, Long> getNormalizedStartActivities(DfgMsd dfm) {
	    Map<String, Long> startActivities = new HashMap<>();
	    for (int activityIndex : dfm.getStartActivities()) {
	        String activityName = dfm.getActivityOfIndex(activityIndex);
	        long cardinality = dfm.getStartActivities().getCardinalityOf(activityIndex);
	        startActivities.put(activityName, cardinality);
	    }
	    return startActivities;
	}

	private Map<String, Long> getNormalizedEndActivities(DfgMsd dfm) {
	    Map<String, Long> endActivities = new HashMap<>();
	    for (int activityIndex : dfm.getEndActivities()) {
	        String activityName = dfm.getActivityOfIndex(activityIndex);
	        long cardinality = dfm.getEndActivities().getCardinalityOf(activityIndex);
	        endActivities.put(activityName, cardinality);
	    }
	    return endActivities;
	}

	private List<String> getNormalizedDfgEdges(DfgMsd dfm) {
	    List<String> edges = new ArrayList<>();
	    IntGraph g = dfm.getDirectlyFollowsGraph();
	    
	    for (long edge : g.getEdges()) {
	        long weight = g.getEdgeWeight(edge);
	        if (weight > 0) {
	            int source = g.getEdgeSource(edge);
	            int target = g.getEdgeTarget(edge);
	            String sourceActivity = dfm.getActivityOfIndex(source);
	            String targetActivity = dfm.getActivityOfIndex(target);
	            
	            String normalized = sourceActivity + "->" + targetActivity + ":" + weight;
	            edges.add(normalized);
	        }
	    }
	    
	    Collections.sort(edges);
	    return edges;
	}

	@Override
	public PortObjectSpec getSpec() {
		// TODO Auto-generated method stub
		if(m_spec!=null)
			return m_spec;
		return new DfgMsdPortObjectSpec();
	}

	public void setSpec(PortObjectSpec spec) {
		m_spec = (DfgMsdPortObjectSpec) spec;
	}
	@Override
	public JComponent[] getViews() {
		return new JComponent[] { };
	}


	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO create own way to save the PortObject due to simplicity
		// we use ObjectOutputStream to save this object
		out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
		/*result.writeInt(dfm.getNumberOfActivities());
		for (String e : dfm.getAllActivities()) {
			result.writeUTF(e + "\n");
		}

		result.writeInt(dfm.getStartActivities().setSize());
		for (int activityIndex : dfm.getStartActivities()) {
			result.writeUTF(activityIndex + "x" + dfm.getStartActivities().getCardinalityOf(activityIndex) + "\n");
		}

		result.writeInt(dfm.getEndActivities().setSize());
		for (int activityIndex : dfm.getEndActivities()) {
			result.writeUTF(activityIndex + "x" + dfm.getEndActivities().getCardinalityOf(activityIndex) + "\n");
		}

		//dfg-edges
		{
			IntGraph g = dfm.getDirectlyFollowsGraph();
			long edges = 0;
			for (Iterator<Long> iterator = g.getEdges().iterator(); iterator.hasNext();) {
				iterator.next();
				edges++;
			}
			result.writeLong(edges);
			for (long edge : g.getEdges()) {
				long v = g.getEdgeWeight(edge);
				if (v > 0) {
					int source = g.getEdgeSource(edge);
					int target = g.getEdgeTarget(edge);
					result.writeUTF(source + ">");
					result.writeUTF(target + "x");
					result.writeLong(v);
				}
			}
		}

		//msd-edges
		if (dfm instanceof DfgMsd) {
			IntGraph g = ((DfgMsd) dfm).getMinimumSelfDistanceGraph();
			long edges = 0;
			for (Iterator<Long> iterator = g.getEdges().iterator(); iterator.hasNext();) {
				iterator.next();
				edges++;
			}
			result.writeLong(edges);
			for (long edge : g.getEdges()) {
				long v = g.getEdgeWeight(edge);
				if (v > 0) {
					int source = g.getEdgeSource(edge);
					int target = g.getEdgeTarget(edge);
					result.writeUTF(source + ">");
					result.writeUTF(target + "x");
					result.writeLong(v);
				}
			}
		}
		result.close();*/
		BufferedWriter result = new BufferedWriter(new OutputStreamWriter(out, CHARSET), BUFFER_SIZE);
		result.append(dfm.getNumberOfActivities() + "\n");
		for (String e : dfm.getAllActivities()) {
			result.append(e + "\n");
		}

		result.append(dfm.getStartActivities().setSize() + "\n");
		for (int activityIndex : dfm.getStartActivities()) {
			result.append(activityIndex + "x" + dfm.getStartActivities().getCardinalityOf(activityIndex) + "\n");
		}

		result.append(dfm.getEndActivities().setSize() + "\n");
		for (int activityIndex : dfm.getEndActivities()) {
			result.append(activityIndex + "x" + dfm.getEndActivities().getCardinalityOf(activityIndex) + "\n");
		}

		//dfg-edges
		{
			IntGraph g = dfm.getDirectlyFollowsGraph();
			long edges = 0;
			for (Iterator<Long> iterator = g.getEdges().iterator(); iterator.hasNext();) {
				iterator.next();
				edges++;
			}
			result.append(edges + "\n");
			for (long edge : g.getEdges()) {
				long v = g.getEdgeWeight(edge);
				if (v > 0) {
					int source = g.getEdgeSource(edge);
					int target = g.getEdgeTarget(edge);
					result.append(source + ">");
					result.append(target + "x");
					result.append(v + "\n");
				}
			}
		}

		//msd-edges
		if (dfm instanceof DfgMsd) {
			IntGraph g = ((DfgMsd) dfm).getMinimumSelfDistanceGraph();
			long edges = 0;
			for (Iterator<Long> iterator = g.getEdges().iterator(); iterator.hasNext();) {
				iterator.next();
				edges++;
			}
			result.append(edges + "\n");
			for (long edge : g.getEdges()) {
				long v = g.getEdgeWeight(edge);
				if (v > 0) {
					int source = g.getEdgeSource(edge);
					int target = g.getEdgeTarget(edge);
					result.append(source + ">");
					result.append(target + "x");
					result.append(v + "\n");
				}
			}
		}

		result.flush();
		result.close();
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		ZipEntry nextEntry = in.getNextEntry();
		if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
			throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
		}

		//DfgMsdPortObject result = null;
		try {
			// they put layout information into context, if we want to show the them, 
			// we need to keep the context the same in load and save program. But how to do this??
			// that's why there is context in portObject. If we also save the context, what can be done??
			DfgMsd dfg = DfgMsdImportPlugin.readFile(in);
			//result = new DfgMsdPortObject(dfg);
			//result.setSpec(spec);
			dfm = dfg;
			setSpec(spec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		in.close();

	}

	public static final class DfgMsdPortObjectSerializer extends AbstractPortObjectSerializer<DfgMsdPortObject> {
	}

	@Override
	public Map<String, List<?>> getJSON() {
		
		Map<String, List<?>> result = new HashMap<>();	
		Map<Integer, String> vertexToIDMapping = new HashMap<>();
		
		int numActivities = dfm.getNumberOfActivities();
		int startVertex = numActivities;
		int endVertex = numActivities + 1;
				
		vertexToIDMapping.put(startVertex, new NodeID().toString());
		vertexToIDMapping.put(endVertex, new NodeID().toString());
		
		List<Node> nodes = new ArrayList<>();
					
		for(int node : dfm.getDirectlyFollowsGraph().getNodes()) {
			NodeID id = new NodeID();
			vertexToIDMapping.put(node, id.toString());
			nodes.add(new PlaceNode(id.toString(), "activity", dfm.getActivityOfIndex(node), false, false));
		}
		
		nodes.add(new PlaceNode(vertexToIDMapping.get(startVertex), "artificial start", "start", true, false));
		nodes.add(new PlaceNode(vertexToIDMapping.get(endVertex), "artificial end", "end", false, true));
		
		result.put("nodes", nodes);

		List<LinkWithFrequency> links = new ArrayList<>();
		
		for (long edge : dfm.getDirectlyFollowsGraph().getEdges()) {
			int source = dfm.getDirectlyFollowsGraph().getEdgeSource(edge);
			int target = dfm.getDirectlyFollowsGraph().getEdgeTarget(edge);
			int weight = (int)dfm.getDirectlyFollowsGraph().getEdgeWeight(edge);
			links.add(new LinkWithFrequency(vertexToIDMapping.get(source), vertexToIDMapping.get(target), weight));
		}
				
		for (int start : dfm.getStartActivities()) {
			int source = startVertex;
			int target = start;
			int weight = (int) dfm.getStartActivities().getCardinalityOf(start);
			links.add(new LinkWithFrequency(vertexToIDMapping.get(source), vertexToIDMapping.get(target), weight));
		}
		
		for (int end : dfm.getEndActivities()) {
			int source = end;
			int target = endVertex;
			int weight = (int) dfm.getEndActivities().getCardinalityOf(end);
			links.add(new LinkWithFrequency(vertexToIDMapping.get(source), vertexToIDMapping.get(target), weight));
		}
				
		result.put("links", links);
		
		return result;
	}
}
