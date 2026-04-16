package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.DfgMsdPortObject;

public class JSGraphVizDFGNodeFactory extends DefaultNodeFactory {

    private static final String INPUT_PORT_GROUP = "dfg-input";
    private static final String IMAGE_OUTPUT_PORT_GROUP = "image-output";

    public JSGraphVizDFGNodeFactory() {
        super(
            DefaultNode.create()
                .name("DFG Viewer")
                .icon("./dfg.png")
                .shortDescription("Open an interactive viewer for directly-follows graphs and optionally export an SVG image.")
                .fullDescription("""
                    <p>
                    This node opens an interactive viewer for directly-follows graphs (DFGs).
                    </p>
                    <p>
                    The node is intended primarily for inspection in the KNIME view. If needed, an SVG image can also be created via the optional output port.
                    </p>
                    """)
                .sinceVersion(2, 0, 0)
                .dynamicPorts(p -> p
                    .addInputPortGroup(INPUT_PORT_GROUP, in -> in
                        .name("Directly-Follows Graph")
                        .description("a directly follows graph")
                        .fixed(DfgMsdPortObject.TYPE))
                    .addOutputPortGroup(IMAGE_OUTPUT_PORT_GROUP, out -> out
                        .name("Image")
                        .description("an optional SVG image export")
                        .optional()
                        .supportedTypes(ImagePortObject.TYPE)))
                .model(m -> m
                    .withoutParameters()
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSGraphVizDFGNodeFactory.class, "DFG view"))
                .nodeType(NodeType.Visualizer));
    }
}
