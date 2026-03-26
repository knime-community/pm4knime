package org.pm4knime.node.io.processtree.reader;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.util.defaultnode.ReaderNodeModel;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;

public class ProcessTreeReaderNodeFactory extends DefaultNodeFactory {

    public ProcessTreeReaderNodeFactory() {
        super(
            DefaultNode.create()
                .name("Process Tree Reader")
                .icon("../../read.png")
                .shortDescription("Import a process tree from a PTML file.")
                .fullDescription("Import a process tree from a PTML file.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addOutputPort("Process Tree", "a process tree", ProcessTreePortObject.TYPE))
                .model(m -> m
                    .parametersClass(ReaderNodeSettings.class)
                    .configure((i, o) -> ReaderNodeModel.configure(i, o,
                        new ProcessTreeReaderNodeModel(ReaderNodeSettings.class)))
                    .execute((i, o) -> ReaderNodeModel.execute(i, o,
                        new ProcessTreeReaderNodeModel(ReaderNodeSettings.class))))
//                .addView(v -> ModernViews.graph(v, ProcessTreeReaderNodeFactory.class, "Process tree view"))
                .nodeType(NodeType.Source));
    }
}
