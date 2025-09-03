package org.pm4knime.node.conversion.table2log;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.XLogPortObject;



public class Table2XLogConverterNodeFactory extends WebUINodeFactory<Table2XLogConverterNodeModel> {

	Table2XLogConverterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Table to Event Log")
			.icon("../category-conversion.png")
			.shortDescription("This node converts a KNIME Data Table into an event log.")
			.fullDescription("This node converts a KNIME Data Table into an event log.")
			.modelSettingsClass(Table2XLogConverterNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Event Log", XLogPortObject.TYPE, "an event log")//
			.nodeType(NodeType.Manipulator)
			.build();



	public Table2XLogConverterNodeFactory() {
		super(CONFIG);
	}


	protected Table2XLogConverterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public Table2XLogConverterNodeModel createNodeModel() {
		node = new Table2XLogConverterNodeModel(Table2XLogConverterNodeSettings.class);
		return node;
	}

}