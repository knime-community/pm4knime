package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.base.data.xml.SvgCell;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.node.DefaultModel;
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
            final var inputType = PortTypeRegistry.getInstance().getPortType(portObject.getClass());
            final var imagePort = render(portObject, inputType, i.getExecutionContext());
            o.setOutData(0, imagePort);
            o.setInternalData(portObject);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }       
    }
    
    static ImagePortObject render(final AbstractJSONPortObject portObject, final PortType inputType,
        final ExecutionContext exec) throws Exception {

        return null;
    }
}
