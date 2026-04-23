package org.pm4knime.node.io.log.writer.xes;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.XLogPortObject;

public class XESWriterNodeFactory extends DefaultNodeFactory {

    public XESWriterNodeFactory() {
        super(
            DefaultNode.create()
                .name("XES Writer")
                .icon("../../../write.png")
                .shortDescription("This node exports an event log into an XES file.")
                .fullDescription("""
                        <p>
                        This node exports an event log into an XES file or a compressed XES file (gz).
                        </p>
                        """)
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addInputPort("Event Log", "an event log", XLogPortObject.TYPE))
                .model(m -> m
                    .parametersClass(XESWriterNodeSettings.class)
                    .configure(XESWriterNodeModel::configure)
                    .execute(XESWriterNodeModel::execute))
                .nodeType(NodeType.Sink));
    }
}
