package org.pm4knime.node.conformance.table.fitness;

import org.knime.core.node.BufferedDataTable;
import org.knime.node.parameters.NodeParameters;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.RepResultPortObjectTable;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;



public class FitnessCheckerNodeFactory extends WebUINodeFactory<FitnessCheckerNodeModel> {

	FitnessCheckerNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Alignment-Based Fitness Evaluator")
			.icon("../../category-conformance.png")
			.shortDescription("Based on the replay result, this node outputs the statistical fitness information.")
			.fullDescription("This node computes the statistical fitness information based on the result of alignment-based replayer.")//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Replay Result", RepResultPortObjectTable.TYPE ,"replay result")//
			.addOutputPort("Fitness Statinfo", BufferedDataTable.TYPE, "fitness statistical information")//
			.nodeType(NodeType.Other)
			.build();



	public FitnessCheckerNodeFactory() {
		super(CONFIG);
	}


	protected FitnessCheckerNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public FitnessCheckerNodeModel createNodeModel() {
		node = new FitnessCheckerNodeModel(EmptyNodeSettings.class);
		return node;
	}
}