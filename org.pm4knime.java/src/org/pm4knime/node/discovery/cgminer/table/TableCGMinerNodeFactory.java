package org.pm4knime.node.discovery.cgminer.table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.CausalGraphPortObject;



public class TableCGMinerNodeFactory extends WebUINodeFactory<TableCGMinerNodeModel> implements WizardNodeFactoryExtension<TableCGMinerNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	TableCGMinerNodeModel node;

	public static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Causal Graph Miner")
			.icon("../../category-discovery.png")
			.shortDescription("This node implements the first step of the Hybrid Miner to discover a causal graph from an event table.")
			.fullDescription("This node implements the first step of the Hybrid Miner to discover a causal graph from an event table. "
					+ "A causal graph consists of nodes representing activities and directed arcs connecting them. "
					+ "We distinguish three types of these arcs: (1) strong dependencies are represented by blue solid arcs (certain arcs); "
					+ "(2) weak dependencies are represented by red dotted arcs (uncertain arcs); (3) long-term dependencies are represented by orange solid arcs.") 
			.modelSettingsClass(TableCGMinerNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Causal Graph", CausalGraphPortObject.TYPE, "a causal graph")//
			.nodeType(NodeType.Learner)
			.build();


	public TableCGMinerNodeFactory() {
		super(CONFIG);
	}


	protected TableCGMinerNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public TableCGMinerNodeModel createNodeModel() {
		node = new TableCGMinerNodeModel(TableCGMinerNodeSettings.class);
		return node;
	}

}

