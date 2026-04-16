package org.pm4knime.node.discovery.inductiveminer.Table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.portobject.ProcessTreePortObject;

public class InductiveMinerTableNodeFactory extends DefaultNodeFactory {

    public InductiveMinerTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Inductive Miner (Table)")
                .icon("../../category-discovery.png")
                .shortDescription("This node implements the Inductive Miner to discover a process tree from an event table.")
                .fullDescription("This node is used to discover a process tree from an event table.\r\n"
                    + "        A process tree is a block-structured process model where the (inner) nodes are operators (such as sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
                    + "        The inductive miner guarantees the discovery of a sound process model. \r\n"
                    + "        The discovered process tree can be converted into a Petri net.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Process Tree", "a process tree", ProcessTreePortObject.TYPE))
                .model(m -> m
                    .parametersClass(InductiveMinerTableNodeSettings.class)
                    .configure((i, o) -> DefaultTableMinerNodeModel.configure(i, o,
                        new InductiveMinerTableNodeModel(InductiveMinerTableNodeSettings.class)))
                    .execute((i, o) -> DefaultTableMinerNodeModel.execute(i, o,
                        new InductiveMinerTableNodeModel(InductiveMinerTableNodeSettings.class))))
//                .addView(v -> ModernViews.graph(v, InductiveMinerTableNodeFactory.class, "Process tree view"))
                .nodeType(NodeType.Learner));
    }
}
