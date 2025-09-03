package org.pm4knime.node.visualizations.jsgraphviz.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.knime.node.parameters.NodeParameters;
import org.knime.js.core.JSONViewContent;
import org.knime.js.core.node.AbstractImageWizardNodeModel;


@SuppressWarnings("restriction")
public abstract class WebUIJSViewNodeModel<S extends NodeParameters, REP extends JSONViewContent, VAL extends JSONViewContent> extends
AbstractImageWizardNodeModel<REP, VAL> {

	private S m_modelSettings;
	
	private final Class<S> m_modelSettingsClass;

	
    protected WebUIJSViewNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes, final String viewName, final Class<S> modelSettingsClass) {
        super(inPortTypes, outPortTypes, viewName);
        m_modelSettingsClass = modelSettingsClass;
        try {
            m_modelSettings = m_modelSettingsClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate settings class: " + m_modelSettingsClass.getName(), e);
        }
    }
    
    @Override
    protected final ImagePortObject createImagePortObjectFromView(final String imageData, final String error) throws IOException {
        String xmlPrimer = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        String svgPrimer = xmlPrimer
            + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">";
        String image = imageData;
        if (image != null && (image.length() < 4 || !image.substring(0, 4).equalsIgnoreCase("<svg"))) {
            image = null;
        }
        String errorText = error;
        if (StringUtils.isEmpty(image)) {
            if (StringUtils.isEmpty(errorText)) {
                errorText = "JavaScript returned nothing. Possible implementation error.";
            }
            image = "<svg width=\"600px\" height=\"40px\">"
                    + "<text x=\"0\" y=\"20\" font-family=\"sans-serif;\" font-size=\"10\">"
               + "SVG retrieval failed: " + errorText + "</text></svg>";
        }
        image = svgPrimer + image;
        InputStream is = new ByteArrayInputStream(image.getBytes("UTF-8"));
        ImagePortObjectSpec imageSpec = new ImagePortObjectSpec(SvgCell.TYPE);
        return new ImagePortObject(new SvgImageContent(is), imageSpec);
    }

    @Override
    protected String getExtractImageMethodName() {
        return "getSVG";
    }
    

    @Override
    protected final PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return configure(inSpecs, m_modelSettings);
    }

    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return configure(inSpecs, m_modelSettings);
    }

    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, final S modelSettings)
        throws InvalidSettingsException {
        var tableSpecs = Stream.of(inSpecs).map(DataTableSpec.class::cast).toArray(DataTableSpec[]::new);
        return configure(tableSpecs, modelSettings);
    }

    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs, final S modelSettings)
        throws InvalidSettingsException {
        throw new NotImplementedException("NodeModel.configure() implementation missing!");
    }

    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec, final S modelSettings)
        throws Exception {
        var tables = Stream.of(inObjects).map(BufferedDataTable.class::cast).toArray(BufferedDataTable[]::new);
        return execute(tables, exec, modelSettings);
    }

    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec,
        final S modelSettings) throws Exception {//NOSONAR
        throw new NotImplementedException("NodeModel.execute() implementation missing!");
    }


    @Override
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_modelSettings != null) {
        	NodeParametersUtil.saveSettings(m_modelSettingsClass, m_modelSettings, settings);
        }
    }
    

    @Override
    protected final void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        
    }


    @Override
    protected final void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_modelSettings = NodeParametersUtil.loadSettings(settings, m_modelSettingsClass);
    }

}
