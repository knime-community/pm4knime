package org.pm4knime.node.discovery.inductiveminer.Table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;



public class InductiveMinerTableNodeFactory extends WebUINodeFactory<InductiveMinerTableNodeModel> implements WizardNodeFactoryExtension<InductiveMinerTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	InductiveMinerTableNodeModel node;

	public static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Inductive Miner (Table)")
			.icon("../../category-discovery.png")
			.shortDescription("This node implements the Inductive Miner to discover a process tree from an event table.")
			.fullDescription("This node is used to discover a process tree from an event table.\r\n"
					+ "        A process tree is a block-structured process model where the (inner) nodes are operators (such as sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
					+ "        The inductive miner guarantees the discovery of a sound process model. \r\n"
					+ "		The discovered process tree can be converted into a Petri net.") 
			.modelSettingsClass(InductiveMinerTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Process Tree", ProcessTreePortObject.TYPE, "a process tree")//
			.nodeType(NodeType.Learner)
			.build();


	public InductiveMinerTableNodeFactory() {
		super(CONFIG);
	}


	protected InductiveMinerTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public InductiveMinerTableNodeModel createNodeModel() {
		node = new InductiveMinerTableNodeModel(InductiveMinerTableNodeSettings.class);
		return node;
	}

}

