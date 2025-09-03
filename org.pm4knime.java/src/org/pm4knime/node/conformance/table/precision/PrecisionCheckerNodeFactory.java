package org.pm4knime.node.conformance.table.precision;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.knime.core.node.NodeFactory.NodeType;
import org.pm4knime.portobject.RepResultPortObjectTable;



public class PrecisionCheckerNodeFactory extends WebUINodeFactory<PrecisionCheckerNodeModel> {

	PrecisionCheckerNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Alignment-Based Precision Evaluator")
			.icon("../../category-conformance.png")
			.shortDescription("Based on the replay result, this node computes the statistical precision information.")
			.fullDescription("This node computes the statistical precision information based on the result of alignment-based replayer.\r\n"
					+ "        Conceptually, the precision of a process model compared to one event log is supposed to be (1) high when the model \r\n"
					+ "        allows for few traces not seen in the log; and (2) low when it allows for many traces not seen in the log.")
			.modelSettingsClass(PrecisionCheckerNodeSettings.class)//
			.addInputPort("Replay Result", RepResultPortObjectTable.TYPE ,"replay result")//
			.addOutputPort("Precision Statinfo", BufferedDataTable.TYPE, "precision statistical information")//
			.nodeType(NodeType.Other)
			.build();



	public PrecisionCheckerNodeFactory() {
		super(CONFIG);
	}


	protected PrecisionCheckerNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public PrecisionCheckerNodeModel createNodeModel() {
		node = new PrecisionCheckerNodeModel(PrecisionCheckerNodeSettings.class);
		return node;
	}

}

