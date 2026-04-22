package org.pm4knime.node.conversion.log2table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.XLogPortObject;

public class XLog2TableConverterNodeFactory extends DefaultNodeFactory {

    public XLog2TableConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Event Log to Table")
                .icon("../category-conversion.png")
                .shortDescription("This node converts an event log into a KNIME Data Table.")
                .fullDescription("This node converts an event log into a KNIME Data Table.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Event Log", "an event log", XLogPortObject.TYPE)
                    .addOutputTable("Event Table", "an event table")
                    .addOutputTable("Case Table", "a case table"))
                .model(m -> m
                    .withoutParameters()
                    .configure(XLog2TableConverterNodeModel::configure)
                    .execute(XLog2TableConverterNodeModel::execute))
                .nodeType(NodeType.Manipulator));
    }
}
