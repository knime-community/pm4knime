package org.pm4knime.node.visualizations.jsgraphviz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.JSONViewContent;
import org.knime.js.core.node.AbstractImageWizardNodeModel;
import org.pm4knime.portobject.AbstractJSONPortObject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.gson.Gson;

@SuppressWarnings("restriction")
final class JSGraphVizImageNodeModel extends AbstractImageWizardNodeModel<JSGraphVizImageNodeModel.JsonRepresentation,
    JSGraphVizImageNodeModel.EmptyValue> {

    private static final PortType[] OUT_TYPES = {ImagePortObject.TYPE};

    private JSGraphVizImageNodeModel(final PortType inputType) {
        super(new PortType[]{inputType}, OUT_TYPES, "Graph JS View");
    }

    static ImagePortObject render(final AbstractJSONPortObject portObject, final PortType inputType,
        final ExecutionContext exec) throws Exception {

        final var model = new JSGraphVizImageNodeModel(inputType);
        final var outputs = model.execute(new PortObject[]{portObject}, exec);
        return (ImagePortObject)outputs[0];
    }

    @Override
    protected ImagePortObject createImagePortObjectFromView(final String imageData, final String error)
        throws IOException {

        final String xmlPrimer = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        final String svgPrimer = xmlPrimer
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
        final InputStream is = new ByteArrayInputStream(image.getBytes("UTF-8"));
        final ImagePortObjectSpec imageSpec = new ImagePortObjectSpec(SvgCell.TYPE);
        return new ImagePortObject(new SvgImageContent(is), imageSpec);
    }

    @Override
    protected String getExtractImageMethodName() {
        return "getSVG";
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[]{new ImagePortObjectSpec(SvgCell.TYPE)};
    }

    @Override
    protected void performExecuteCreateView(final PortObject[] inObjects, final ExecutionContext exec)
        throws Exception {

        final var portObject = (AbstractJSONPortObject)inObjects[0];
        getViewRepresentation().setJSONString(portObject.getJSON());
    }

    @Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {

        return new PortObject[]{svgImageFromView};
    }

    @Override
    protected void performReset() {
    }

    @Override
    protected void useCurrentValueAsDefault() {
    }

    @Override
    protected boolean generateImage() {
        return true;
    }

    @Override
    public JsonRepresentation createEmptyViewRepresentation() {
        return new JsonRepresentation();
    }

    @Override
    public EmptyValue createEmptyViewValue() {
        return new EmptyValue();
    }

    @Override
    public String getJavascriptObjectID() {
        return "org.pm4knime.node.visualizations.jsgraphviz.component";
    }

    @Override
    public boolean isHideInWizard() {
        return false;
    }

    @Override
    public void setHideInWizard(final boolean hide) {
    }

    @Override
    public ValidationError validateViewValue(final EmptyValue viewContent) {
        return null;
    }

    @Override
    public void saveCurrentValue(final NodeSettingsWO content) {
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static final class JsonRepresentation extends JSONViewContent {

        private static final String JSON = "json";
        private String json;

        @Override
        public void saveToNodeSettings(final NodeSettingsWO settings) {
            if (json != null) {
                settings.addString(JSON, json);
            }
        }

        @Override
        public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
            try {
                json = settings.getString(JSON);
            } catch (Exception ex) {
                json = null;
            }
        }

        void setJSONString(final Map<String, ? extends Object> json) {
            this.json = new Gson().toJson(json);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof JsonRepresentation other)) {
                return false;
            }
            return Objects.equals(json, other.json);
        }

        @Override
        public int hashCode() {
            return Objects.hash(json);
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static final class EmptyValue extends JSONViewContent {

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof EmptyValue;
        }

        @Override
        public int hashCode() {
            return EmptyValue.class.hashCode();
        }

        @Override
        public void saveToNodeSettings(final NodeSettingsWO settings) {
        }

        @Override
        public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        }
    }
}
