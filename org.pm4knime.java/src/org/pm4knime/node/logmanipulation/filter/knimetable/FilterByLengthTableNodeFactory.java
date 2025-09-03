package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;



public final class FilterByLengthTableNodeFactory extends WebUINodeFactory<FilterByLengthTableNodeModel> {

	FilterByLengthTableNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Filter Event Table by Length")
			.icon("../../category-manipulation.png")
			.shortDescription("This node filters the traces based on their length.")
			.fullDescription("This node filters the traces based on their length.")
			.modelSettingsClass(FilterByLengthTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"The event table to be filtered.")//
			.addOutputPort("Table", BufferedDataTable.TYPE, "The filtered event table.")//
			.nodeType(NodeType.Manipulator)
			.build();


	public FilterByLengthTableNodeFactory() {
		super(CONFIG);
	}


	protected FilterByLengthTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public FilterByLengthTableNodeModel createNodeModel() {
		node = new FilterByLengthTableNodeModel(FilterByLengthTableNodeSettings.class);
		return node;
	}

}

