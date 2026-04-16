package org.pm4knime.node.discovery.cgminer.table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.portobject.CausalGraphPortObject;

public class TableCGMinerNodeFactory extends DefaultNodeFactory {

    public TableCGMinerNodeFactory() {
        super(
            DefaultNode.create()
                .name("Causal Graph Miner")
                .icon("../../category-discovery.png")
                .shortDescription("This node implements the first step of the Hybrid Miner to discover a causal graph from an event table.")
                .fullDescription("This node implements the first step of the Hybrid Miner to discover a causal graph from an event table. "
                    + "A causal graph consists of nodes representing activities and directed arcs connecting them. "
                    + "We distinguish three types of these arcs: (1) strong dependencies are represented by blue solid arcs (certain arcs); "
                    + "(2) weak dependencies are represented by red dotted arcs (uncertain arcs); (3) long-term dependencies are represented by orange solid arcs.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Causal Graph", "a causal graph", CausalGraphPortObject.TYPE))
                .model(m -> m
                    .parametersClass(TableCGMinerNodeSettings.class)
                    .configure((i, o) -> DefaultTableMinerNodeModel.configure(i, o,
                        new TableCGMinerNodeModel(TableCGMinerNodeSettings.class)))
                    .execute((i, o) -> DefaultTableMinerNodeModel.execute(i, o,
                        new TableCGMinerNodeModel(TableCGMinerNodeSettings.class))))
//                .addView(v -> ModernViews.graph(v, TableCGMinerNodeFactory.class, "Causal graph view"))
                .nodeType(NodeType.Learner));
    }
}
