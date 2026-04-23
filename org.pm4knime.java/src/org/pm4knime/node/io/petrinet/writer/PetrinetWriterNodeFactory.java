package org.pm4knime.node.io.petrinet.writer;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.PetriNetPortObject;

public class PetrinetWriterNodeFactory extends DefaultNodeFactory {

    public PetrinetWriterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Petri Net Writer")
                .icon("../../write.png")
                .shortDescription("Export a Petri net into a PNML file.")
                .fullDescription("""
                        <p>
                        This nodes exports a Petri net into a PMML file.
                        </p>
                        """)
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addInputPort("Petri Net", "a Petri net", PetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(PetrinetWriterNodeSettings.class)
                    .configure(PetrinetWriterNodeModel::configure)
                    .execute(PetrinetWriterNodeModel::execute))
                .nodeType(NodeType.Sink));
    }
}
