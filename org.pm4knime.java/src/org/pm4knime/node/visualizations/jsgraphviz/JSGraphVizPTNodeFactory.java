package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class JSGraphVizPTNodeFactory extends DefaultNodeFactory {

    public JSGraphVizPTNodeFactory() {
        super(
            DefaultNode.create()
                .name("Process Tree To Image")
                .icon("./tree.png")
                .shortDescription("JavaScript Visualizer for Process Trees")
                .fullDescription("This node implements a JavaScript visualization of Process Trees.\r\n"
                    + "    <br />\r\n"
                    + "    A process tree is a block-structured process model where the (inner) nodes are operators (sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
                    + "    <br /> \r\n"
                    + "    The \"seq\" operator executes its children from right to left.\r\n"
                    + "    <br />\r\n"
                    + "    The \"xor\" operator executes one of its children.\r\n"
                    + "    <br />\r\n"
                    + "    The \"and\" operator executes the children in parallel.\r\n"
                    + "    <br />\r\n"
                    + "    The \"xor loop\" operator models a do-redo loop. The first child is used as the do part, while an exclusive choice between the other children is used as the redo part.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Process Tree", "a process tree", ProcessTreePortObject.TYPE)
                    .addOutputPort("Image", "an SVG image", ImagePortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSGraphVizPTNodeFactory.class, "Process tree view"))
                .nodeType(NodeType.Visualizer));
    }
}
