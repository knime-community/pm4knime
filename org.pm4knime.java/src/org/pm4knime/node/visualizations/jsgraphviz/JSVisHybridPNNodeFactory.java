package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class JSVisHybridPNNodeFactory extends DefaultNodeFactory {

    public JSVisHybridPNNodeFactory() {
        super(
            DefaultNode.create()
                .name("Hybrid Petri Net To Image")
                .icon("./process.png")
                .shortDescription("JavaScript Visualizer for hybrid Petri nets.")
                .fullDescription("This node implements a JavaScript visualization of hybrid Petri nets. By default, informal edges are colored as follows: blue arcs for certain edges, red arcs for uncertain edges, and yellow arcs for long-term dependency edges.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Hybrid Petri Net", "a hybrid Petri net", HybridPetriNetPortObject.TYPE)
                    .addOutputPort("Image", "an SVG image", ImagePortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(GraphVisualizerModel::configure)
                    .execute(GraphVisualizerModel::execute))
                .addView(v -> ModernViews.graph(v, JSVisHybridPNNodeFactory.class, "Hybrid Petri net view"))
                .nodeType(NodeType.Visualizer));
    }
}
