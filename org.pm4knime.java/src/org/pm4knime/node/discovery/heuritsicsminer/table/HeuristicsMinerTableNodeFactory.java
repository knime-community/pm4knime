package org.pm4knime.node.discovery.heuritsicsminer.table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;

public class HeuristicsMinerTableNodeFactory extends DefaultNodeFactory {

    public HeuristicsMinerTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Heuristics Miner")
                .icon("../../category-discovery.png")
                .shortDescription("This node implements the Heuristics Miner to discover a Petri net from an event table.")
                .fullDescription("This node implements the Heuristics Miner to discover a Petri net from an event table.\r\n"
                    + "            The Heuristics Miner discovers a heuristics net, which is a directed graph with activities as nodes and edges connecting nodes to model dependencies between activities.\r\n"
                    + "            The discovered heuristics net is converted into a Petri net. "
                    + PetriNetPortObject.PETRI_NET_TEXT)
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Petri Net", "a Petri net", PetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(HeuristicsMinerTableNodeSettings.class)
                    .configure((i, o) -> DefaultTableMinerNodeModel.configure(i, o,
                        new HeuristicsMinerTableNodeModel(HeuristicsMinerTableNodeSettings.class)))
                    .execute((i, o) -> DefaultTableMinerNodeModel.execute(i, o,
                        new HeuristicsMinerTableNodeModel(HeuristicsMinerTableNodeSettings.class))))
//                .addView(v -> ModernViews.graph(v, HeuristicsMinerTableNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Learner));
    }
}
