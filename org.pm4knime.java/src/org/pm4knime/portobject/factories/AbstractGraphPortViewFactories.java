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
import org.pm4knime.portobject.AbstractJSONPortObject;

@SuppressWarnings("restriction")
public abstract class AbstractGraphPortViewFactories<T extends AbstractJSONPortObject> {

    protected PortView createPortObjectView(final T obj) {
        return new PortView() {

            @Override
            public Page getPage() {
                return Page.create()
                    .fromFile()
                    .bundleClass(getBundleClass())
                    .basePath(".")
                    .relativeFilePath("js-src/dist/src/views/jsgraphviz/index.html")
                    .addResourceDirectory("js-src/dist/assets");
            }

            @Override
            public Optional<InitialDataService<Object>> createInitialDataService() {
                return Optional.of(
                    InitialDataService.builder(() -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("kind", "graph");
                        data.put("graph", getGraphData(obj));
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
    
    protected static void register(Class<?> portClass, String portName, PortViewFactory<?> view_factory) {
    	
    	PortViewManager.registerPortViews(
    			portClass,
                List.of(
                    new PortViewManager.PortViewDescriptor(portName, view_factory)
                ),
                List.of(0),
                List.of(0)
            );
	}

    protected Map<String, Object> getGraphData(final T obj) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>)(Map<?, ?>)obj.getJSON();
        return result;
    }

    protected abstract Class<?> getBundleClass();
}