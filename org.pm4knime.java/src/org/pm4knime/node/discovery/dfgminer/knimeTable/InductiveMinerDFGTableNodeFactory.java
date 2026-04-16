package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;

public final class InductiveMinerDFGTableNodeFactory extends DefaultNodeFactory {

    public InductiveMinerDFGTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Inductive Miner (DFG)")
                .icon("../../category-discovery.png")
                .shortDescription("This node implements the second step of the Inductive Miner to discover a process tree from a directly-follows graph.")
                .fullDescription("This node  is used to discover a process tree from a directly-follows graph (DFG).\r\n"
                    + "        A process tree is a block-structured process model where the (inner) nodes are operators (such as sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
                    + "        The inductive miner guarantees the discovery of a sound process model. \r\n"
                    + "        The discovered process tree can be converted into a Petri net.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Directly-Follows Graph", "a directly-follows graph", DfgMsdPortObject.TYPE)
                    .addOutputPort("Process Tree", "a process tree", ProcessTreePortObject.TYPE))
                .model(m -> m
                    .parametersClass(InductiveMinerDFGTableNodeSettings.class)
                    .configure((i, o) -> InductiveMinerDFGTableNodeModel.configure(i, o,
                        new InductiveMinerDFGTableNodeModel(InductiveMinerDFGTableNodeSettings.class)))
                    .execute((i, o) -> InductiveMinerDFGTableNodeModel.execute(i, o,
                        new InductiveMinerDFGTableNodeModel(InductiveMinerDFGTableNodeSettings.class))))
//                .addView(v -> ModernViews.graph(v, InductiveMinerDFGTableNodeFactory.class, "Process tree view"))
                .nodeType(NodeType.Learner));
    }
}
