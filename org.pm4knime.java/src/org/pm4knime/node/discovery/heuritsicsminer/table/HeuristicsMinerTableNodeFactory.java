package org.pm4knime.node.discovery.heuritsicsminer.table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.PetriNetPortObject;



public class HeuristicsMinerTableNodeFactory extends WebUINodeFactory<HeuristicsMinerTableNodeModel> implements WizardNodeFactoryExtension<HeuristicsMinerTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	
	HeuristicsMinerTableNodeModel node;

	public static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Heuristics Miner")
			.icon("../../category-discovery.png")
			.shortDescription("This node implements the Heuristics Miner to discover a Petri net from an event table.")
			.fullDescription("This node implements the Heuristics Miner to discover a Petri net from an event table.\r\n"
					+ "			The Heuristics Miner discovers a heuristics net, which is a directed graph with activities as nodes and edges connecting nodes to model dependencies between activities.\r\n"
					+ "			The discovered heuristics net is converted into a Petri net. "
					+ PetriNetPortObject.PETRI_NET_TEXT)// 
			.modelSettingsClass(HeuristicsMinerTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Petri Net", PetriNetPortObject.TYPE, "a Petri net")//
			.nodeType(NodeType.Learner)
			.build();


	public HeuristicsMinerTableNodeFactory() {
		super(CONFIG);
	}


	protected HeuristicsMinerTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public HeuristicsMinerTableNodeModel createNodeModel() {
		node = new HeuristicsMinerTableNodeModel(HeuristicsMinerTableNodeSettings.class);
		return node;
	}


}

