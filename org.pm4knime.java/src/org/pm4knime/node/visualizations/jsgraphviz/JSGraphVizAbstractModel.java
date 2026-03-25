package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.base.data.xml.SvgCell;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.pm4knime.node.visualizations.jsgraphviz.util.WebUIJSViewNodeModel;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;


public class JSGraphVizAbstractModel extends
    WebUIJSViewNodeModel<EmptyNodeSettings, JSGraphVizViewRepresentation, JSGraphVizViewValue>
    implements PortObjectHolder {

    private static final PortType[] OUT_TYPES = {ImagePortObject.TYPE};

    private AbstractJSONPortObject port_obj;

    public JSGraphVizAbstractModel(final PortType[] in_types, final String view_name,
        final Class<EmptyNodeSettings> modelSettingsClass) {
        super(in_types, OUT_TYPES, view_name, modelSettingsClass);
    }

    public ImagePortObject createImageFromView(final AbstractJSONPortObject portObject, final ExecutionContext exec)
        throws Exception {
        port_obj = portObject;
        final var outputs = super.execute(new PortObject[]{portObject}, exec);
        return (ImagePortObject)outputs[0];
    }


	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
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
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs, final EmptyNodeSettings modelSettings) throws InvalidSettingsException {
		
		PortObjectSpec imageSpec = new ImagePortObjectSpec(SvgCell.TYPE);
        
        return new PortObjectSpec[]{imageSpec};
	}

	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			
			port_obj = (AbstractJSONPortObject) inObjects[0];
			representation.setJSONString(port_obj.getJSON());
		}

	}

	@Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	
	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {port_obj};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		port_obj = (AbstractJSONPortObject) portObjects[0];
	}
	
	@Override
    protected boolean generateImage() {
        return true;
    }
	
	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{svgImageFromView};
    }
	
}
