package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;



public final class InductiveMinerDFGTableNodeFactory extends WebUINodeFactory<InductiveMinerDFGTableNodeModel> implements WizardNodeFactoryExtension<InductiveMinerDFGTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    private InductiveMinerDFGTableNodeModel node;

    private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Inductive Miner (DFG)")
			.icon("../../category-discovery.png")
			.shortDescription("This node implements the second step of the Inductive Miner to discover a process tree from a directly-follows graph.")
			.fullDescription("This node  is used to discover a process tree from a directly-follows graph (DFG).\r\n"
					+ "        A process tree is a block-structured process model where the (inner) nodes are operators (such as sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
					+ "        The inductive miner guarantees the discovery of a sound process model. \r\n"
					+ "		The discovered process tree can be converted into a Petri net.")
			.modelSettingsClass(InductiveMinerDFGTableNodeSettings.class)//
			.addInputPort("Directly-Follows Graph", DfgMsdPortObject.TYPE ,"a directly-follows graph")//
			.addOutputPort("Process Tree", ProcessTreePortObject.TYPE, "a process tree")//
			.nodeType(NodeType.Learner)
			.build();


	public InductiveMinerDFGTableNodeFactory() {
		super(CONFIG);
	}


	protected InductiveMinerDFGTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public InductiveMinerDFGTableNodeModel createNodeModel() {
		node = new InductiveMinerDFGTableNodeModel(InductiveMinerDFGTableNodeSettings.class);
		return node;
	}


}

