package org.pm4knime.node.io.log.writer.mxml;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.XLogPortObject;


public class MXMLWriterNodeFactory extends WebUINodeFactory<MXMLWriterNodeModel> {

    private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder() //
        .name("MXML Writer") //
        .icon("../../../write.png") //
        .shortDescription("This node exports an event log into an MXML file.") //
        .fullDescription("""
                <p>
                This node exports an event log into an MXML file or a compressed MXML file (gz).
                </p>
                """) //
        .modelSettingsClass(MXMLWriterNodeSettings.class) //
        .addInputPort("Event Log", XLogPortObject.TYPE, "an event log")
        .nodeType(NodeType.Sink)
		.build();

    public MXMLWriterNodeFactory() {
        super(CONFIG);
    }

    @Override
    public MXMLWriterNodeModel createNodeModel() {
        return new MXMLWriterNodeModel(CONFIG);
    }


}

