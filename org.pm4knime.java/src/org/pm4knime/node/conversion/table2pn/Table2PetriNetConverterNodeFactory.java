package org.pm4knime.node.conversion.table2pn;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class Table2PetriNetConverterNodeFactory extends DefaultNodeFactory {

    public Table2PetriNetConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Table to Petri Net")
                .icon("../category-conversion.png")
                .shortDescription("Convert a KNIME Data Table into a Petri net")
                .fullDescription("This node converts a KNIME Data Table into a Petri net. "
                    + PetriNetPortObject.PETRI_NET_TEXT)
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Petri net", "a Petri net", PetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(Table2PetriNetConverterNodeModel::configure)
                    .execute(Table2PetriNetConverterNodeModel::execute))
//                .addView(v -> ModernViews.graph(v, Table2PetriNetConverterNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Manipulator));
    }
}
