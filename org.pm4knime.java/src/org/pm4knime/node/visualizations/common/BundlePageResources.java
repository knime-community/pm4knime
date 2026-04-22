package org.pm4knime.node.visualizations.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.FileLocator;
import org.knime.core.node.NodeLogger;
import org.knime.core.webui.page.Page;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.pm4knime.util.PM4KNIMEPlugin;

@SuppressWarnings("restriction")
public final class BundlePageResources {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(BundlePageResources.class);
    private static final String BASE_PATH = "js-src/dist";
    private static final String ASSETS_DIR = "assets";

    private BundlePageResources() {
    }

    public static Page createPage(final String relativePagePath) {
        try {
            return Page.create()
                .fromString(() -> readBundleText(pathInBundle(relativePagePath)))
                .relativePath(relativePagePath)
                .addResources(
                    relativePath -> openAsset(relativePagePath, relativePath),
                    ASSETS_DIR,
                    true
                );
        } catch (RuntimeException | Error ex) {
            LOGGER.error("PM4KNIME bundle page creation failed: " + buildDiagnostics(relativePagePath), ex);
            throw ex;
        }
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

    private static InputStream openAsset(final String relativePagePath, final String relativeAssetPath) {
        final String bundlePath = pathInBundle(ASSETS_DIR + "/" + relativeAssetPath);
        final InputStream in = openBundleResource(bundlePath);
        if (in == null) {
            LOGGER.warn("PM4KNIME bundle asset not found for page " + relativePagePath + ": " + bundlePath);
        }
        return in;
    }

    private static InputStream openBundleResource(final String bundlePath) {
        final var bundle = FrameworkUtil.getBundle(PM4KNIMEPlugin.class);
        if (bundle == null) {
            throw new IllegalStateException("Cannot resolve PM4KNIME bundle");
        }

        final URL entry = bundle.getEntry(bundlePath);
        if (entry == null) {
            return null;
        }

        try {
            return entry.openStream();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to open bundle resource: " + bundlePath, ex);
        }
    }

    private static String buildDiagnostics(final String relativePagePath) {
        final var bundleClass = PM4KNIMEPlugin.class;
        final var bundle = FrameworkUtil.getBundle(bundleClass);
        final var entryDot = bundle != null ? bundle.getEntry(".") : null;
        final var entryDist = bundle != null ? bundle.getEntry(BASE_PATH) : null;
        final var entryIndex = bundle != null ? bundle.getEntry(pathInBundle(relativePagePath)) : null;
        final var entryAssets = bundle != null ? bundle.getEntry(pathInBundle(ASSETS_DIR)) : null;

        final StringBuilder dbg = new StringBuilder();
        dbg.append("page=").append(relativePagePath);
        dbg.append(" | bundleClass=").append(bundleClass.getName());
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

    private static String safeResolve(final URL url) {
        if (url == null) {
            return "null";
        }
        try {
            return String.valueOf(FileLocator.resolve(url));
        } catch (Exception ex) {
            return "FAIL(" + ex.getClass().getName() + ":" + ex.getMessage() + ")";
        }
    }

    private static String safeReadHead(final URL url) {
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
}
