package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.DfgMsdPortObject;

public class DfgMinerTableNodeFactory extends DefaultNodeFactory {

    public DfgMinerTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("DFG Miner")
                .icon("../../category-discovery.png")
                .shortDescription("This node implements the first step of the Inductive Miner to discover a directly-follows graph from an event table.")
                .fullDescription("  This node is used to discover a directly-follows graph (DFG) from an event table. \r\n"
                    + "        A DFG consists of nodes representing activities and directed edges connecting nodes to model the directly-follows relations between activities. \r\n"
                    + "        The green nodes represent the start activities, while the red ones are for the end activities.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Directly-Follows Graph", "a directly-follows graph", DfgMsdPortObject.TYPE))
                .model(m -> m
                    .parametersClass(DefaultTableMinerSettings.class)
                    .configure((i, o) -> DefaultTableMinerNodeModel.configure(i, o,
                        new DfgMinerTableNodeModel(DefaultTableMinerSettings.class)))
                    .execute((i, o) -> DefaultTableMinerNodeModel.execute(i, o,
                        new DfgMinerTableNodeModel(DefaultTableMinerSettings.class))))
                .addView(v -> ModernViews.graph(v, DfgMinerTableNodeFactory.class, "DFG view"))
                .nodeType(NodeType.Learner));
    }
}
