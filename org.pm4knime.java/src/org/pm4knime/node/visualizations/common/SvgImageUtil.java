package org.pm4knime.node.visualizations.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;

public final class SvgImageUtil {

    private static final String XML_PRIMER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final String SVG_PRIMER = XML_PRIMER
        + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" "
        + "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">";

    private SvgImageUtil() {
    }

    public static ImagePortObject fromSvg(final String svg) throws IOException {
        final String normalized = svg != null && svg.trim().startsWith("<svg")
            ? svg
            : "<svg width=\"640\" height=\"60\"><text x=\"16\" y=\"34\" font-size=\"14\" "
                + "font-family=\"Arial, sans-serif\">No SVG content available.</text></svg>";

        final var content = new ByteArrayInputStream((SVG_PRIMER + normalized).getBytes(StandardCharsets.UTF_8));
        final var spec = new ImagePortObjectSpec(SvgCell.TYPE);
        return new ImagePortObject(new SvgImageContent(content), spec);
    }
}
