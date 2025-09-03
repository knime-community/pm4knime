package org.pm4knime.node.io.log.reader.MXMLImporter;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.XLogPortObject;


public class MXMLImporterNodeFactory extends WebUINodeFactory<MXMLImporterNodeModel>  {

	MXMLImporterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("MXML Reader").icon("../../../read.png")
			.shortDescription("This node imports an event log from an MXML file.")
			.fullDescription("This node imports an event log from an MXML file using the Open Naive method.")
			.modelSettingsClass(MXMLImporterNodeSettings.class)
			.addOutputPort("Event Log", XLogPortObject.TYPE, "an event log")
			.nodeType(NodeType.Source).build();

	public MXMLImporterNodeFactory() {
		super(CONFIG);
	}

	protected MXMLImporterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}

	@Override
	public MXMLImporterNodeModel createNodeModel() {
		node = new MXMLImporterNodeModel(MXMLImporterNodeSettings.class);
		return node;
	}
}