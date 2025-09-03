package org.pm4knime.node.logmanipulation.sample.knimetable;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;



public final class SampleLogTableNodeFactory extends WebUINodeFactory<SampleLogTableNodeModel> {

	SampleLogTableNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Event Table Partitioning")
			.icon("../../category-manipulation.png")
			.shortDescription("This node is used to randomly sample a percentage of traces from the event table.")
			.fullDescription("This node is used to randomly sample a percentage of traces from the event table. After sampling, it outputs two event tables: the sampled event table and the event table that contains the removed traces.")
			.modelSettingsClass(SampleLogTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"The event table to be sampled.")//
			.addOutputPort("Table", BufferedDataTable.TYPE, "The sampled event table.")//
			.addOutputPort("Table", BufferedDataTable.TYPE, "The event table that contains the removed traces.")//
			.nodeType(NodeType.Manipulator)
			.build();


	public SampleLogTableNodeFactory() {
		super(CONFIG);
	}


	protected SampleLogTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public SampleLogTableNodeModel createNodeModel() {
		node = new SampleLogTableNodeModel(SampleLogTableNodeSettings.class);
		return node;
	}

}

