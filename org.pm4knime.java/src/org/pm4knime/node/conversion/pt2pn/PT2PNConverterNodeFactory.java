package org.pm4knime.node.conversion.pt2pn;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class PT2PNConverterNodeFactory extends DefaultNodeFactory {

    public PT2PNConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Process Tree to Petri Net")
                .icon("icon/category-conversion.png")
                .shortDescription("Convert a process tree into a Petri net.")
                .fullDescription(
                    "This node converts a process tree into a Petri net. \r\n"
                        + "        No configuration is needed to complete the conversion. \r\n"
                        + "        <br />\r\n"
                        + "    A process tree is a block-structured process model where the (inner) nodes are "
                        + "operators (sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
                        + "    <br /> \r\n"
                        + PetriNetPortObject.PETRI_NET_TEXT)
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Process Tree", "a process tree", ProcessTreePortObject.TYPE)
                    .addOutputPort("Petri net", "a Petri net", PetriNetPortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(PT2PNConverterNodeModel::configure)
                    .execute(PT2PNConverterNodeModel::execute))
                .addView(v -> ModernViews.graph(v, PT2PNConverterNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Manipulator));
    }
}
