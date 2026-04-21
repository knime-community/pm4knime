package org.pm4knime.node.visualizations.common;

import org.knime.core.webui.page.FromFilePage;
import org.knime.node.DefaultView;
import org.pm4knime.util.PM4KNIMEPlugin;

public final class ModernViews {

    private static final String BASE_PATH = "js-src/dist";
    private static final String ASSETS_DIR = "assets";
    private static final String GRAPH_PAGE = "src/views/jsgraphviz/index.html";
    private static final String BPMN_PAGE = "src/views/bpmn/index.html";

    private ModernViews() {
    }

    public static DefaultView graph(final DefaultView.RequireViewParameters v,
        final Class<?> bundleClass2, final String description) {

        return v.withoutParameters() //
            .description(description) //
            .page(p -> addAssets(p.fromFile().bundleClass(PM4KNIMEPlugin.class).basePath(BASE_PATH).relativeFilePath(GRAPH_PAGE)))
            .initialData(JsonViewData::graphInitialData);
    }

    public static DefaultView bpmn(final DefaultView.RequireViewParameters v,
        final Class<?> bundleClass2, final String description) {

        return v.withoutParameters() //
            .description(description) //
            .page(p -> addBpmnResources(
                p.fromFile().bundleClass(PM4KNIMEPlugin.class).basePath(BASE_PATH).relativeFilePath(BPMN_PAGE)))
            .initialData(JsonViewData::bpmnInitialData);
    }

    private static FromFilePage addAssets(final FromFilePage page) {
        return page.addResourceDirectory(ASSETS_DIR);
    }

    private static FromFilePage addBpmnResources(final FromFilePage page) {
        return addAssets(page);
    }
}
