package org.pm4knime.node.io.hybridpetrinet.writer;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.HybridPetriNetPortObject;

public class HybridPetrinetWriterNodeFactory extends DefaultNodeFactory {

    public HybridPetrinetWriterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Hybrid Petri Net Writer")
                .icon("../../write.png")
                .shortDescription("Export a hybrid Petri net into a PNML file.")
                .fullDescription("""
                        <p>
                        This nodes exports a hybrid Petri net into a PMML file.
                        </p>
                        """)
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addInputPort("Hybrid Petri Net", "a hybrid Petri net", HybridPetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(HybridPetrinetWriterNodeSettings.class)
                    .configure(HybridPetrinetWriterNodeModel::configure)
                    .execute(HybridPetrinetWriterNodeModel::execute))
                .nodeType(NodeType.Sink));
    }
}
