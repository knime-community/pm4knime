package org.pm4knime.portobject.factories;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.FileLocator;
import org.knime.core.node.NodeLogger;
import org.knime.core.webui.data.InitialDataService;
import org.knime.core.webui.data.RpcDataService;
import org.knime.core.webui.node.port.PortView;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.knime.core.webui.page.Page;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.util.PM4KNIMEPlugin;

@SuppressWarnings("restriction")
public abstract class AbstractGraphPortViewFactories<T extends AbstractJSONPortObject> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(AbstractGraphPortViewFactories.class);
    private static final AtomicBoolean PAGE_DIAGNOSTICS_LOGGED = new AtomicBoolean(false);
    private static final String BASE_PATH = "js-src/dist";
    private static final String GRAPH_PAGE = "src/views/jsgraphviz/index.html";
    private static final String ASSETS_DIR = "assets";

    protected PortView createPortObjectView(final T obj) {
        return new PortView() {

            @Override
            public Page getPage() {
                final String diagnostics = buildPageDiagnostics();

                if (PAGE_DIAGNOSTICS_LOGGED.compareAndSet(false, true)) {
                    LOGGER.info("PM4KNIME graph view diagnostics: " + diagnostics);
                }

                try {
                    return Page.create()
                        .fromString(() -> readBundleText(pathInBundle(GRAPH_PAGE)))
                        .relativePath(GRAPH_PAGE)
                        .addResources(
                            relativePath -> openBundleResource(pathInBundle(ASSETS_DIR + "/" + relativePath)),
                            ASSETS_DIR,
                            true
                        );
                } catch (RuntimeException | Error ex) {
                    LOGGER.error("PM4KNIME graph view page creation failed: " + diagnostics, ex);
                    throw ex;
                }
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

    private static String buildPageDiagnostics() {
        final var bundleClass = PM4KNIMEPlugin.class;
        final var bundle = FrameworkUtil.getBundle(bundleClass);
        final var entryDot = bundle != null ? bundle.getEntry(".") : null;
        final var entryDist = bundle != null ? bundle.getEntry(BASE_PATH) : null;
        final var entryIndex = bundle != null ? bundle.getEntry(BASE_PATH + "/" + GRAPH_PAGE) : null;
        final var entryAssets = bundle != null ? bundle.getEntry(BASE_PATH + "/" + ASSETS_DIR) : null;

        final StringBuilder dbg = new StringBuilder();
        dbg.append("bundleClass=").append(bundleClass.getName());
        dbg.append(" | bundle=").append(bundle);
        dbg.append(" | bundleLocation=").append(bundle != null ? bundle.getLocation() : "null");
        dbg.append(" | bundleClassPath=")
            .append(bundle != null ? bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH) : "null");
        dbg.append(" | entryDot=").append(entryDot);
        dbg.append(" | entryDist=").append(entryDist);
        dbg.append(" | entryIndex=").append(entryIndex);
        dbg.append(" | entryAssets=").append(entryAssets);
        dbg.append(" | resolveDot=").append(safeResolve(entryDot));
        dbg.append(" | resolveDist=").append(safeResolve(entryDist));
        dbg.append(" | resolveIndex=").append(safeResolve(entryIndex));
        dbg.append(" | resolveAssets=").append(safeResolve(entryAssets));
        dbg.append(" | htmlHead=").append(safeReadHead(entryIndex));
        return dbg.toString();
    }

    private static String safeResolve(final java.net.URL url) {
        if (url == null) {
            return "null";
        }
        try {
            return String.valueOf(FileLocator.resolve(url));
        } catch (Exception ex) {
            return "FAIL(" + ex.getClass().getName() + ":" + ex.getMessage() + ")";
        }
    }

    private static String safeReadHead(final java.net.URL url) {
        if (url == null) {
            return "null";
        }
        try (var in = url.openStream()) {
            final byte[] bytes = in.readNBytes(180);
            return sanitize(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            return "FAIL(" + ex.getClass().getName() + ":" + ex.getMessage() + ")";
        }
    }

    private static String sanitize(final String text) {
        return text.replace("\r", " ")
            .replace("\n", " ")
            .replace("|", "/");
    }

    private static String pathInBundle(final String relativePath) {
        return BASE_PATH + "/" + relativePath;
    }

    private static String readBundleText(final String bundlePath) {
        try (var in = openBundleResource(bundlePath)) {
            if (in == null) {
                throw new IllegalStateException("Missing bundle resource: " + bundlePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read bundle resource: " + bundlePath, ex);
        }
    }

    private static InputStream openBundleResource(final String bundlePath) {
        final var bundle = FrameworkUtil.getBundle(PM4KNIMEPlugin.class);
        if (bundle == null) {
            throw new IllegalStateException("Cannot resolve PM4KNIME bundle");
        }

        final var entry = bundle.getEntry(bundlePath);
        if (entry == null) {
            return null;
        }

        try {
            return entry.openStream();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to open bundle resource: " + bundlePath, ex);
        }
    }

}
