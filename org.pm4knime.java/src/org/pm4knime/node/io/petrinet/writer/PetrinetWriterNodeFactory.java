package org.pm4knime.node.io.petrinet.writer;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.pm4knime.portobject.PetriNetPortObject;


public class PetrinetWriterNodeFactory extends WebUINodeFactory<PetrinetWriterNodeModel> {

    private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder() //
        .name("Petri Net Writer") //
        .icon("../../write.png") //
        .shortDescription("Export a Petri net into a PNML file.") //
        .fullDescription("""
                <p>
                This nodes exports a Petri net into a PMML file.
                </p>
                """) //
        .modelSettingsClass(PetrinetWriterNodeSettings.class) //
        .addInputPort("Petri Net", PetriNetPortObject.TYPE, "a Petri net")
        .nodeType(NodeType.Sink)
		.build();

    public PetrinetWriterNodeFactory() {
        super(CONFIG);
    }

    @Override
    public PetrinetWriterNodeModel createNodeModel() {
        return new PetrinetWriterNodeModel(CONFIG);
    }


}

