package org.pm4knime.node.io.hybridpetrinet.writer;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.HybridPetriNetPortObject;


public class HybridPetrinetWriterNodeFactory extends WebUINodeFactory<HybridPetrinetWriterNodeModel> {

    private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder() //
        .name("Hybrid Petri Net Writer") //
        .icon("../../write.png") //
        .shortDescription("Export a hybrid Petri net into a PNML file.") //
        .fullDescription("""
                <p>
                This nodes exports a hybrid Petri net into a PMML file.
                </p>
                """) //
        .modelSettingsClass(HybridPetrinetWriterNodeSettings.class) //
        .addInputPort("Hybrid Petri Net", HybridPetriNetPortObject.TYPE, "a hybrid Petri net")
        .nodeType(NodeType.Sink)
		.build();

    public HybridPetrinetWriterNodeFactory() {
        super(CONFIG);
    }

    @Override
    public HybridPetrinetWriterNodeModel createNodeModel() {
        return new HybridPetrinetWriterNodeModel(CONFIG);
    }


}

