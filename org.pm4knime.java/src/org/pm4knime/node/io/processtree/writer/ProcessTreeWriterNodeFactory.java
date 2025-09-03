package org.pm4knime.node.io.processtree.writer;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.ProcessTreePortObject;


public class ProcessTreeWriterNodeFactory extends WebUINodeFactory<ProcessTreeWriterNodeModel> {

    private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder() //
        .name("ProcessTree Writer") //
        .icon("../../write.png") //
        .shortDescription("Export a process tree into a PTML file.") //
        .fullDescription("""
                <p>
                This nodes exports a process tree into a PTML file.
                </p>
                """) //
        .modelSettingsClass(ProcessTreeWriterNodeSettings.class) //
        .addInputPort("Process Tree", ProcessTreePortObject.TYPE, "a process tree")
        .nodeType(NodeType.Sink)
		.build();

    public ProcessTreeWriterNodeFactory() {
        super(CONFIG);
    }

    @Override
    public ProcessTreeWriterNodeModel createNodeModel() {
        return new ProcessTreeWriterNodeModel(CONFIG);
    }


}

