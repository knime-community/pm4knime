package org.pm4knime.node.conversion.log2table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;


public class XLog2TableConverterNodeFactory extends WebUINodeFactory<XLog2TableConverterNodeModel> {

	XLog2TableConverterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Event Log to Table")
			.icon("../category-conversion.png")
			.shortDescription("This node converts an event log into a KNIME Data Table.")
			.fullDescription("This node converts an event log into a KNIME Data Table.")
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Event Log", XLogPortObject.TYPE, "an event log")//
			.addOutputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.nodeType(NodeType.Manipulator)
			.build();



	public XLog2TableConverterNodeFactory() {
		super(CONFIG);
	}


	protected XLog2TableConverterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public XLog2TableConverterNodeModel createNodeModel() {
		node = new XLog2TableConverterNodeModel(EmptyNodeSettings.class);
		return node;
	}

}
