package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;



public final class FilterByFrequencyTableNodeFactory extends WebUINodeFactory<FilterByFrequencyTableNodeModel> {

	FilterByFrequencyTableNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Filter Event Table by Frequency")
			.icon("../../category-manipulation.png")
			.shortDescription("This node filters the traces based on their frequency in the event table.")
			.fullDescription("This node filters the traces based on their frequency in the event table. \r\n"
					+ "        Traces are always sorted based on the frequencies of the corresponding trace variants, and the most frequent trace variants are kept/removed. \r\n"
					+ "        The filtering strategy is used to determine whether to keep or to remove the most frequent trace variants. \r\n"
					+ "        The percentage of trace variants to be kept/removed is determined by the two filtering options: filtering type and and filtering threshold.")
			.modelSettingsClass(FilterByFrequencyTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"The event table to be filtered.")//
			.addOutputPort("Table", BufferedDataTable.TYPE, "The filtered event table.")//
			.nodeType(NodeType.Manipulator)
			.build();


	public FilterByFrequencyTableNodeFactory() {
		super(CONFIG);
	}


	protected FilterByFrequencyTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public FilterByFrequencyTableNodeModel createNodeModel() {
		node = new FilterByFrequencyTableNodeModel(FilterByFrequencyTableNodeSettings.class);
		return node;
	}

}

