package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.PetriNetPortObject;



public class DfgMinerTableNodeFactory extends WebUINodeFactory<DfgMinerTableNodeModel> implements WizardNodeFactoryExtension<DfgMinerTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	
	DfgMinerTableNodeModel node;

	public static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("DFG Miner")
			.icon("../../category-discovery.png")
			.shortDescription("This node implements the first step of the Inductive Miner to discover a directly-follows graph from an event table.")
			.fullDescription("  This node is used to discover a directly-follows graph (DFG) from an event table. \r\n"
					+ "        A DFG consists of nodes representing activities and directed edges connecting nodes to model the directly-follows relations between activities. \r\n"
					+ "        The green nodes represent the start activities, while the red ones are for the end activities.") 
			.modelSettingsClass(DefaultTableMinerSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Directly-Follows Graph", DfgMsdPortObject.TYPE, "a directly-follows graph")//
			.nodeType(NodeType.Learner)
			.build();


	public DfgMinerTableNodeFactory() {
		super(CONFIG);
	}


	protected DfgMinerTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public DfgMinerTableNodeModel createNodeModel() {
		node = new DfgMinerTableNodeModel(DefaultTableMinerSettings.class);
		return node;
	}


}

