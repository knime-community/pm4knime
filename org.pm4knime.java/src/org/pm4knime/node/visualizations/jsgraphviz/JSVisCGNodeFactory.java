package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.CausalGraphPortObject;

public class JSVisCGNodeFactory extends DefaultNodeFactory {

    private static final String INPUT_PORT_GROUP = "cg-input";
    private static final String IMAGE_OUTPUT_PORT_GROUP = "image-output";

    public JSVisCGNodeFactory() {
        super(
            DefaultNode.create()
                .name("Causal Graph Viewer")
                .icon("./dfg.png")
                .shortDescription("Open an interactive viewer for causal graphs and optionally export an SVG image.")
                .fullDescription("""
                    <p>
                    This node opens an interactive viewer for causal graphs.
                    </p>
                    <p>
                    The node is intended primarily for inspection in the KNIME view. If needed, an SVG image can also be created via the optional output port.
                    </p>
                    <p>
                    Activities are shown as nodes. Strong causal dependencies are drawn in blue, uncertain dependencies in red, and long-term dependencies in yellow.
                    </p>
                    """)
                .sinceVersion(2, 0, 0)
                .dynamicPorts(p -> p
                    .addInputPortGroup(INPUT_PORT_GROUP, in -> in
                        .name("Causal Graph")
                        .description("a causal graph")
                        .fixed(CausalGraphPortObject.TYPE))
                    .addOutputPortGroup(IMAGE_OUTPUT_PORT_GROUP, out -> out
                        .name("Image")
                        .description("an optional SVG image export")
                        .optional()
                        .supportedTypes(ImagePortObject.TYPE)))
                .model(m -> m
                    .withoutParameters()
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSVisCGNodeFactory.class, "Causal graph view"))
                .nodeType(NodeType.Visualizer));
    }
}
