package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class JSGraphVizDFGNodeFactory extends DefaultNodeFactory {

    public JSGraphVizDFGNodeFactory() {
        super(
            DefaultNode.create()
                .name("DFG To Image")
                .icon("./dfg.png")
                .shortDescription("JavaScript Visualizer for Directly-Follows Graphs")
                .fullDescription("This node implements a JavaScript visualization of directly follows graphs (DFGs). \r\n"
                    + "            <br/>\r\n"
                    + "            The green nodes are the start activities and the red activities are the end activity. \r\n"
                    + "            The edges are annotated by the absolute frequencies of the directly follows relations between the activities.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Directly-Follows Graph", "a directly follows graph", DfgMsdPortObject.TYPE)
                    .addOutputPort("Image", "an SVG image", ImagePortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSGraphVizDFGNodeFactory.class, "DFG view"))
                .nodeType(NodeType.Visualizer));
    }
}
