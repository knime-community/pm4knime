package org.pm4knime.node.io.petrinet.reader;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.defaultnode.ReaderNodeModel;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;

public class PetrinetReaderNodeFactory extends DefaultNodeFactory {

    public PetrinetReaderNodeFactory() {
        super(
            DefaultNode.create()
                .name("Petri Net Reader")
                .icon("../../read.png")
                .shortDescription("Import a Petri net from a PNML file.")
                .fullDescription(
                    "This node imports a Petri net from a PNML file. A Petri net is a directed bipartite graph used "
                        + "to model processes. It consists of places, transitions, and directed arcs connecting them. "
                        + "A place is enabled if it it contains at least one token. A transition can only fire if all "
                        + "incoming places are enabled. After firing a transition, a token is consumed from all of its "
                        + "incoming places, and a token is produced in all of its outgoing places. The initial marking "
                        + "indicates the initial state of the Petri net. Places that belong to the initial marking are "
                        + "marked by green tokens inside them. The final marking denotes the final state of the Petri "
                        + "net. Places within the final marking are highlighted with a heavier border.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addOutputPort("Petri net", "a Petri net", PetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(ReaderNodeSettings.class)
                    .configure((i, o) -> ReaderNodeModel.configure(i, o,
                        new PetrinetReaderNodeModel(ReaderNodeSettings.class)))
                    .execute((i, o) -> ReaderNodeModel.execute(i, o,
                        new PetrinetReaderNodeModel(ReaderNodeSettings.class))))
                .addView(v -> ModernViews.graph(v, PetrinetReaderNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Source));
    }
}
