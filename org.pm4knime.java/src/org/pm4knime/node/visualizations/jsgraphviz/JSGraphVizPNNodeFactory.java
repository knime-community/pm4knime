package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class JSGraphVizPNNodeFactory extends DefaultNodeFactory {

    public JSGraphVizPNNodeFactory() {
        super(
            DefaultNode.create()
                .name("Petri Net To Image")
                .icon("./process.png")
                .shortDescription("JavaScript Visualizer for Petri Nets")
                .fullDescription("This node implements a JavaScript visualization of Petri nets.\r\n"
                    + "    <br/>\r\n"
                    + "    A Petri net is a directed bipartite graph that visualizes all possible traces (order of executed activities). A Petri net consists of places, transitions, and directed arcs that connect them.  \r\n"
                    + "    Arcs can either connect places with transitions or transitions with places. Places can contain tokens. The placement of tokens defines the state of the Petri net. There is a start and a final state.\r\n"
                    + "    The start state has only a token in the green place (initial place). The goal state has only a token in the double margin place (final place).\r\n"
                    + "    A place is enabled if it it contains at least one token. A transition can only fire if all incoming places are enabled. \r\n"
                    + "    After firing a transition, a token is consumed from all of its incoming places, and a token is produced in all of its outgoing places.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Petri Net", "a Petri net", PetriNetPortObject.TYPE)
                    .addOutputPort("Image", "an SVG image", ImagePortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSGraphVizPNNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Visualizer));
    }
}
