package org.pm4knime.node.conversion.table2hpn;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class Table2HybridPetriNetConverterNodeFactory extends DefaultNodeFactory {

    public Table2HybridPetriNetConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Table to Hybrid Petri Net")
                .icon("../category-conversion.png")
                .shortDescription("Convert a KNIME Data Table into a hybrid Petri net")
                .fullDescription("This node converts a KNIME Data Table into a hybrid Petri net. "
                    + HybridPetriNetPortObject.HYBRID_PETRI_NET_TEXT)
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Hybrid Petri Net", "a hybrid Petri net", HybridPetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(Table2HybridPetriNetConverterNodeModel::configure)
                    .execute(Table2HybridPetriNetConverterNodeModel::execute))
                .addView(v -> ModernViews.graph(v, Table2HybridPetriNetConverterNodeFactory.class,
                    "Hybrid Petri net view"))
                .nodeType(NodeType.Manipulator));
    }
}
