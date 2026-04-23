package org.pm4knime.node.io.log.writer.mxml;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.XLogPortObject;

public class MXMLWriterNodeFactory extends DefaultNodeFactory {

    public MXMLWriterNodeFactory() {
        super(
            DefaultNode.create()
                .name("MXML Writer")
                .icon("../../../write.png")
                .shortDescription("This node exports an event log into an MXML file.")
                .fullDescription("""
                        <p>
                        This node exports an event log into an MXML file or a compressed MXML file (gz).
                        </p>
                        """)
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addInputPort("Event Log", "an event log", XLogPortObject.TYPE))
                .model(m -> m
                    .parametersClass(MXMLWriterNodeSettings.class)
                    .configure(MXMLWriterNodeModel::configure)
                    .execute(MXMLWriterNodeModel::execute))
                .nodeType(NodeType.Sink));
    }
}
