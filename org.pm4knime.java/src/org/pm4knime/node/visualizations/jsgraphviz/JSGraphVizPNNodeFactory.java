package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.PetriNetPortObject;

public class JSGraphVizPNNodeFactory extends DefaultNodeFactory {

    private static final String INPUT_PORT_GROUP = "pn-input";
    private static final String IMAGE_OUTPUT_PORT_GROUP = "image-output";

    public JSGraphVizPNNodeFactory() {
        super(
            DefaultNode.create()
                .name("Petri Net Viewer")
                .icon("./process.png")
                .shortDescription("Open an interactive viewer for Petri nets and optionally export an SVG image.")
                .fullDescription("""
                    <p>
                    This node opens an interactive viewer for Petri nets.
                    </p>
                    <p>
                    The node is intended primarily for inspection in the KNIME view. If needed, an SVG image can also be created via the optional output port.
                    </p>
                    """)
                .sinceVersion(2, 0, 0)
                .dynamicPorts(p -> p
                    .addInputPortGroup(INPUT_PORT_GROUP, in -> in
                        .name("Petri Net")
                        .description("a Petri net")
                        .fixed(PetriNetPortObject.TYPE))
                    .addOutputPortGroup(IMAGE_OUTPUT_PORT_GROUP, out -> out
                        .name("Image")
                        .description("an optional SVG image export")
                        .optional()
                        .supportedTypes(ImagePortObject.TYPE)))
                .model(m -> m
                    .withoutParameters()
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSGraphVizPNNodeFactory.class, "Petri net view"))
                .nodeType(NodeType.Visualizer));
    }
}
