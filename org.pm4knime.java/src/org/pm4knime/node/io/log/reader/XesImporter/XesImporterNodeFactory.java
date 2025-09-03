package org.pm4knime.node.io.log.reader.XesImporter;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.XLogPortObject;


public class XesImporterNodeFactory extends WebUINodeFactory<XesImporterNodeModel>  {

	XesImporterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("XES Reader").icon("../../../read.png")
			.shortDescription("This node imports an event log from an XES file.")
			.fullDescription("This node imports an event log from an XES file.")
			.modelSettingsClass(XesImporterNodeSettings.class)
			.addOutputPort("Event Log", XLogPortObject.TYPE, "an event log")
			.nodeType(NodeType.Source).build();

	public XesImporterNodeFactory() {
		super(CONFIG);
	}

	protected XesImporterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}

	@Override
	public XesImporterNodeModel createNodeModel() {
		node = new XesImporterNodeModel(XesImporterNodeSettings.class);
		return node;
	}
}