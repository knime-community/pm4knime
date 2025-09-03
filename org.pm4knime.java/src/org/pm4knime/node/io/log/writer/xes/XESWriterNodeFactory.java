package org.pm4knime.node.io.log.writer.xes;

import java.util.Optional;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.XLogPortObject;


public class XESWriterNodeFactory extends WebUINodeFactory<XESWriterNodeModel> {

    private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder() //
        .name("XES Writer") //
        .icon("../../../write.png") //
        .shortDescription("This node exports an event log into an XES file.") //
        .fullDescription("""
                <p>
                This node exports an event log into an XES file or a compressed XES file (gz).
                </p>
                """) //
        .modelSettingsClass(XESWriterNodeSettings.class) //
        .addInputPort("Event Log", XLogPortObject.TYPE, "an event log")
        .nodeType(NodeType.Sink)
		.build();

    public XESWriterNodeFactory() {
        super(CONFIG);
    }

    @Override
    public XESWriterNodeModel createNodeModel() {
        return new XESWriterNodeModel(CONFIG);
    }


}

