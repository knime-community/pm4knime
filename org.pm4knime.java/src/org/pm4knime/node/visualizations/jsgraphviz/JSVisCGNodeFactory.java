package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class JSVisCGNodeFactory extends DefaultNodeFactory {

    public JSVisCGNodeFactory() {
        super(
            DefaultNode.create()
                .name("Causal Graph To Image")
                .icon("./dfg.png")
                .shortDescription("JavaScript Visualizer for Causal Graphs.")
                .fullDescription("This node implements a JavaScript visualization of causal graphs. A causal graph consists of nodes representing activities and two types of directed edges connecting nodes. \r\n"
                    + "            Certain edges (blue by default) represent strong causal dependencies and uncertain edges (red by default) represent weak dependencies. \r\n"
                    + "            A third type of edges is used to represent long-term dependencies (yellow by default).")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Causal Graph", "a causal graph", CausalGraphPortObject.TYPE)
                    .addOutputPort("Image", "an SVG image", ImagePortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSVisCGNodeFactory.class, "Causal graph view"))
                .nodeType(NodeType.Visualizer));
    }
}
