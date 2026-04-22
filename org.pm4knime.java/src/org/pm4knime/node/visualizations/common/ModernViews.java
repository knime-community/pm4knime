package org.pm4knime.node.visualizations.common;

import org.knime.node.DefaultView;

public final class ModernViews {

    private static final String GRAPH_PAGE = "src/views/jsgraphviz/index.html";
    private static final String BPMN_PAGE = "src/views/bpmn/index.html";

    private ModernViews() {
    }

    public static DefaultView graph(final DefaultView.RequireViewParameters v,
        final Class<?> bundleClass2, final String description) {

        return v.withoutParameters() //
            .description(description) //
            .page(p -> BundlePageResources.createPage(GRAPH_PAGE))
            .initialData(JsonViewData::graphInitialData);
    }

    public static DefaultView bpmn(final DefaultView.RequireViewParameters v,
        final Class<?> bundleClass2, final String description) {

        return v.withoutParameters() //
            .description(description) //
            .page(p -> BundlePageResources.createPage(BPMN_PAGE))
            .initialData(JsonViewData::bpmnInitialData);
    }
}
