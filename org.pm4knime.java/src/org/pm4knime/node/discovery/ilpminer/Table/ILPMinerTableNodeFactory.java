package org.pm4knime.node.discovery.ilpminer.Table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.PetriNetPortObject;

public class ILPMinerTableNodeFactory extends DefaultNodeFactory {

    public ILPMinerTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("ILP Miner")
                .icon("../../category-discovery.png")
                .shortDescription("This node implements the ILP Miner to discover a Petri net from an event table.")
                .fullDescription("This node implements the ILP Miner to discover a Petri net from an event table.\r\n"
                    + PetriNetPortObject.PETRI_NET_TEXT)
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Petri Net", "a Petri net", PetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(ILPMinerTableNodeSettings.class)
                    .configure((i, o) -> DefaultTableMinerNodeModel.configure(i, o,
                        new ILPMinerTableNodeModel(ILPMinerTableNodeSettings.class)))
                    .execute((i, o) -> DefaultTableMinerNodeModel.execute(i, o,
                        new ILPMinerTableNodeModel(ILPMinerTableNodeSettings.class))))
                .addView(v -> ModernViews.graph(v, ILPMinerTableNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Learner));
    }
}
