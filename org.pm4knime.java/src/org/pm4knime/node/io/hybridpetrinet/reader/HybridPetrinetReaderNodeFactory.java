package org.pm4knime.node.io.hybridpetrinet.reader;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.util.defaultnode.ReaderNodeModel;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;

public class HybridPetrinetReaderNodeFactory extends DefaultNodeFactory {

    public HybridPetrinetReaderNodeFactory() {
        super(
            DefaultNode.create()
                .name("Hybrid Petri Net Reader")
                .icon("../../read.png")
                .shortDescription("Import a hybrid Petri net from a PNML file.")
                .fullDescription(
                    "Import a hybrid Petri net from a PNML file. A hybrid Petri net is a directed graph used to model "
                        + "processes. It consists of places, transitions, and directed arcs connecting them. A place "
                        + "is enabled if it it contains at least one token. A transition can only fire if all incoming "
                        + "places are enabled. After firing a transition, a token is consumed from all of its incoming "
                        + "places, and a token is produced in all of its outgoing places. The initial marking indicates "
                        + "the initial state of the hybrid Petri net. Places that belong to the initial marking are "
                        + "marked by green tokens inside them. The final marking denotes the final state of the hybrid "
                        + "Petri net. Places within the final marking are highlighted with a heavier border. A hybrid "
                        + "Petri net can also contain arcs directly connecting transitions to indicate informal "
                        + "dependencies between them. We distinguish three types of informal arcs: (1) strong "
                        + "dependencies are represented by blue solid arcs (certain arcs); (2) weak dependencies are "
                        + "represented by red dotted arcs (uncertain arcs); (3) long-term dependencies are represented "
                        + "by orange solid arcs.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addOutputPort("Hybrid Petri Net", "a hybrid Petri net", HybridPetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(ReaderNodeSettings.class)
                    .configure((i, o) -> ReaderNodeModel.configure(i, o,
                        new HybridPetrinetReaderNodeModel(ReaderNodeSettings.class)))
                    .execute((i, o) -> ReaderNodeModel.execute(i, o,
                        new HybridPetrinetReaderNodeModel(ReaderNodeSettings.class))))
                .addView(v -> ModernViews.graph(v, HybridPetrinetReaderNodeFactory.class, "Hybrid Petri net view"))
                .nodeType(NodeType.Source));
    }
}
