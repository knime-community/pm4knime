package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.ProcessTreePortObject;

public class JSGraphVizPTNodeFactory extends DefaultNodeFactory {

    private static final String INPUT_PORT_GROUP = "pt-input";
    private static final String IMAGE_OUTPUT_PORT_GROUP = "image-output";

    public JSGraphVizPTNodeFactory() {
        super(
            DefaultNode.create()
                .name("Process Tree Viewer")
                .icon("./tree.png")
                .shortDescription("Open an interactive viewer for process trees and optionally export an SVG image.")
                .fullDescription("""
                    <p>
                    This node opens an interactive viewer for process trees.
                    </p>
                    <p>
                    The node is intended primarily for inspection in the KNIME view. If needed, an SVG image can also be created via the optional output port.
                    </p>
                    """)
                .sinceVersion(2, 0, 0)
                .dynamicPorts(p -> p
                    .addInputPortGroup(INPUT_PORT_GROUP, in -> in
                        .name("Process Tree")
                        .description("a process tree")
                        .fixed(ProcessTreePortObject.TYPE))
                    .addOutputPortGroup(IMAGE_OUTPUT_PORT_GROUP, out -> out
                        .name("Image")
                        .description("an optional SVG image export")
                        .optional()
                        .supportedTypes(ImagePortObject.TYPE)))
                .model(m -> m
                    .withoutParameters()
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSGraphVizPTNodeFactory.class, "Process tree view"))
                .nodeType(NodeType.Visualizer));
    }
}
