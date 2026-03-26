package org.pm4knime.node.discovery.alpha.table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;

public class AlphaMinerTableNodeFactory extends DefaultNodeFactory {

    public AlphaMinerTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Alpha Miner")
                .icon("../../category-discovery.png")
                .shortDescription("This node implements the Alpha Miner to discover a Petri net from an event table.")
                .fullDescription(" This node implements the Alpha Miner to discover a Petri net from an event table. "
                    + PetriNetPortObject.PETRI_NET_TEXT)
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Petri Net", "a Petri net", PetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(AlphaMinerTableNodeSettings.class)
                    .configure((i, o) -> DefaultTableMinerNodeModel.configure(i, o,
                        new AlphaMinerTableNodeModel(AlphaMinerTableNodeSettings.class)))
                    .execute((i, o) -> DefaultTableMinerNodeModel.execute(i, o,
                        new AlphaMinerTableNodeModel(AlphaMinerTableNodeSettings.class))))
//                .addView(v -> ModernViews.graph(v, AlphaMinerTableNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Learner));
    }
}
