package org.pm4knime.portobject.factories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.knime.core.webui.data.InitialDataService;
import org.knime.core.webui.data.RpcDataService;
import org.knime.core.webui.node.port.PortView;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.knime.core.webui.page.Page;
import org.pm4knime.node.visualizations.common.BundlePageResources;
import org.pm4knime.portobject.BpmnPortObject;

@SuppressWarnings("restriction")
public final class BPMNPortViewFactories {

    private static final BPMNPortViewFactories INSTANCE = new BPMNPortViewFactories();

    static final PortViewFactory<BpmnPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    private static final Class<?> PORT_CLASS = BpmnPortObject.class;
    private static final String PORT_NAME = "BPMN View";
    private static final String BPMN_PAGE = "src/views/bpmn/index.html";

    public static void register() {
        PortViewManager.registerPortViews(
            PORT_CLASS,
            List.of(new PortViewManager.PortViewDescriptor(PORT_NAME, PORT_VIEW_FACTORY)),
            List.of(0),
            List.of(0)
        );
    }

    private PortView createPortObjectView(final BpmnPortObject obj) {
        return new PortView() {

            @Override
            public Page getPage() {
                return BundlePageResources.createPage(BPMN_PAGE);
            }

            @Override
            public Optional<InitialDataService<Object>> createInitialDataService() {
                return Optional.of(
                    InitialDataService.builder(() -> {
                        final Map<String, Object> raw = getBpmnData(obj);

                        final Map<String, Object> data = new HashMap<>();
                        data.put("kind", "bpmn");
                        data.put("xml", firstString(raw.get("xml")));
                        data.put("layouter", firstBoolean(raw.get("layouter")));
                        return (Object)data;
                    }).build()
                );
            }

            @Override
            public Optional<RpcDataService> createRpcDataService() {
                return Optional.empty();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBpmnData(final BpmnPortObject obj) {
        return (Map<String, Object>)(Map<?, ?>)obj.getJSON();
    }

    private static String firstString(final Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            final Object first = list.get(0);
            return first == null ? "" : first.toString();
        }
        return value == null ? "" : value.toString();
    }

    private static boolean firstBoolean(final Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            final Object first = list.get(0);
            if (first instanceof Boolean bool) {
                return bool.booleanValue();
            }
            return Boolean.parseBoolean(String.valueOf(first));
        }
        if (value instanceof Boolean bool) {
            return bool.booleanValue();
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private BPMNPortViewFactories() {
    }
}
