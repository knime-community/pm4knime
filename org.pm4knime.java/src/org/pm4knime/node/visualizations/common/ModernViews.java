package org.pm4knime.node.visualizations.common;

import org.knime.core.webui.page.FromFilePage;
import org.knime.node.DefaultView;

public final class ModernViews {

    private static final String BASE_PATH = ".";
    private static final String ASSETS_DIR = "js-src/dist/assets";
    private static final String GRAPH_PAGE = "js-src/dist/src/views/jsgraphviz/index.html";
    private static final String BPMN_PAGE = "js-src/dist/src/views/bpmn/index.html";

    private ModernViews() {
    }

    public static DefaultView graph(final DefaultView.RequireViewParameters v,
        final Class<?> bundleClass, final String description) {

        return v.withoutParameters() //
            .description(description) //
            .page(p -> addAssets(p.fromFile().bundleClass(bundleClass).basePath(BASE_PATH).relativeFilePath(GRAPH_PAGE)))
            .initialData(JsonViewData::graphInitialData);
    }

    public static DefaultView bpmn(final DefaultView.RequireViewParameters v,
        final Class<?> bundleClass, final String description) {

        return v.withoutParameters() //
            .description(description) //
            .page(p -> addBpmnResources(
                p.fromFile().bundleClass(bundleClass).basePath(BASE_PATH).relativeFilePath(BPMN_PAGE)))
            .initialData(JsonViewData::bpmnInitialData);
    }

    private static FromFilePage addAssets(final FromFilePage page) {
        return page.addResourceDirectory(ASSETS_DIR);
    }

    private static FromFilePage addBpmnResources(final FromFilePage page) {
        return addAssets(page);
    }
}
