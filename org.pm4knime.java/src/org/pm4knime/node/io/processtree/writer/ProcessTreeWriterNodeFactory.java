package org.pm4knime.node.io.processtree.writer;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.ProcessTreePortObject;

public class ProcessTreeWriterNodeFactory extends DefaultNodeFactory {

    public ProcessTreeWriterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Process Tree Writer")
                .icon("../../write.png")
                .shortDescription("Export a process tree into a PTML file.")
                .fullDescription("""
                        <p>
                        This nodes exports a process tree into a PTML file.
                        </p>
                        """)
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addInputPort("Process Tree", "a process tree", ProcessTreePortObject.TYPE))
                .model(m -> m
                    .parametersClass(ProcessTreeWriterNodeSettings.class)
                    .configure(ProcessTreeWriterNodeModel::configure)
                    .execute(ProcessTreeWriterNodeModel::execute))
                .nodeType(NodeType.Sink));
    }
}
