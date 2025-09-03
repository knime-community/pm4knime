package org.pm4knime.node.conformance.table.performance;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.RepResultPortObjectTable;



public class PerformanceCheckerNodeFactory extends WebUINodeFactory<PerformanceCheckerNodeModel> {

	PerformanceCheckerNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Alignment-Based Performance Evaluator")
			.icon("../../category-conformance.png")
			.shortDescription("Based on the replay result, this node computes the statistical performance information.")
			.fullDescription("This node computes the statistical precision information based on the result of alignment-based replayer.\r\n"
					+ "        Conceptually, the precision of a process model compared to one event log is supposed to be (1) high when the model \r\n"
					+ "        allows for few traces not seen in the log; and (2) low when it allows for many traces not seen in the log.")
			.modelSettingsClass(PerformanceCheckerNodeSettings.class)//
			.addInputPort("Replay Result", RepResultPortObjectTable.TYPE ,"replay result")//
			.addOutputPort("Global Performance StatInfo", BufferedDataTable.TYPE, "global performance statistical information.")//
			.addOutputPort("Transition Performance Statistics", BufferedDataTable.TYPE, "performance statistical information for the different transitions in the Petri net (waiting time, synchronization time, and sojourn time).")//
			.addOutputPort("Place Performance Statistics", BufferedDataTable.TYPE, "performance statistical information for the different places in the Petri net (waiting time, synchronization time, and sojourn time).")//
			.nodeType(NodeType.Other)
			.build();



	public PerformanceCheckerNodeFactory() {
		super(CONFIG);
	}


	protected PerformanceCheckerNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public PerformanceCheckerNodeModel createNodeModel() {
		node = new PerformanceCheckerNodeModel(PerformanceCheckerNodeSettings.class);
		return node;
	}

}
