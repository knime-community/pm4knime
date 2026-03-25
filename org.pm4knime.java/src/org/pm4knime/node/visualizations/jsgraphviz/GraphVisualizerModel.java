package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.base.data.xml.SvgCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.node.DefaultModel;
import org.pm4knime.node.visualizations.common.GraphSvgRenderer;
import org.pm4knime.node.visualizations.common.SvgImageUtil;
import org.pm4knime.portobject.AbstractJSONPortObject;

public final class GraphVisualizerModel {

    private GraphVisualizerModel() {
    }

    static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o)
        throws InvalidSettingsException {

        o.setOutSpec(0, new ImagePortObjectSpec(SvgCell.TYPE));
    }

    static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o) {
        try {
            final var portObject = (AbstractJSONPortObject)i.getInPortObject(0);
            final var svg = GraphSvgRenderer.render(portObject);
            o.setOutData(0, SvgImageUtil.fromSvg(svg));
            o.setInternalData(portObject);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
