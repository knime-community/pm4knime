package org.pm4knime.node.discovery.hybridminer;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObject;

public final class HybridMinerNodeFactory extends DefaultNodeFactory {

    public HybridMinerNodeFactory() {
        super(
            DefaultNode.create()
                .name("Hybrid Petri Net Miner")
                .icon("../category-discovery.png")
                .shortDescription("This node implements the second step of the Hybrid Miner to discover a hybrid Petri net from a causal graph.")
                .fullDescription("This node implements the second step of the Hybrid Miner to discover a hybrid Petri net from a causal graph. "
                    + "The hybrid miner converts the edges of the input causal graph into formal places if there is enough evidence in the data justifying adding formal constraints. "
                    + "A hybrid Petri net can also contain arcs directly connecting transitions to indicate informal dependencies between them. "
                    + "We distinguish three types of informal arcs: (1) strong dependencies are represented by blue solid arcs (certain arcs); "
                    + "(2) weak dependencies are represented by red dotted arcs (uncertain arcs); (3) long-term dependencies are represented by orange solid arcs.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Causal Graph", "a causal graph", CausalGraphPortObject.TYPE)
                    .addOutputPort("Hybrid Petri Net", "a hybrid Petri net", HybridPetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(HybridMinerNodeSettings.class)
                    .configure((i, o) -> HybridMinerNodeModel.configure(i, o,
                        new HybridMinerNodeModel(HybridMinerNodeSettings.class)))
                    .execute((i, o) -> HybridMinerNodeModel.execute(i, o,
                        new HybridMinerNodeModel(HybridMinerNodeSettings.class))))
                .addView(v -> ModernViews.graph(v, HybridMinerNodeFactory.class, "Hybrid Petri net view"))
                .nodeType(NodeType.Learner));
    }
}
