package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.knime.base.data.xml.SvgCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.core.node.web.ValidationError;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;



@SuppressWarnings("restriction")
public class TraceVariantVisNodeModel extends AbstractSVGWizardNodeModel<TraceVariantVisViewRepresentation, TraceVariantVisViewValue> implements PortObjectHolder {

	// Input and output port types
	private static PortType[] IN_TYPES = {BufferedDataTable.TYPE};
	private static PortType[] OUT_TYPES = {ImagePortObject.TYPE};
	AbstractJSONPortObject port_obj;
	
	public static final String KEY_TRACE_CLASSIFIER = "Trace Classifier";
	public static final String KEY_EVENT_CLASSIFIER = "Event Classifier";
	public static final String KEY_CLASSIFIER_SET = "Classifier Set";
	
//	protected String t_classifier;
//	protected String e_classifier;
	protected Boolean generate_image = true;
	
	protected BufferedDataTable table;
	
	protected TraceVariantVisNodeSettings m_settings = new TraceVariantVisNodeSettings();
	private final Class<TraceVariantVisNodeSettings> m_settingsClass;

	public TraceVariantVisNodeModel(Class<TraceVariantVisNodeSettings> modelSettingsClass) {
		super(IN_TYPES, OUT_TYPES, "Trace Variant Explorer");
		m_settingsClass = modelSettingsClass;
	}

	@Override
	public TraceVariantVisViewRepresentation createEmptyViewRepresentation() {
		return new TraceVariantVisViewRepresentation();
	}

	@Override
	public TraceVariantVisViewValue createEmptyViewValue() {
		return new TraceVariantVisViewValue();
	}

	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.logviews.tracevariant.component";
	}

	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(TraceVariantVisViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (!inSpecs[0].getClass().equals(DataTableSpec.class))
			throw new InvalidSettingsException("Input is not a valid Table!");
		if(m_settings.e_classifier == null || m_settings.t_classifier == null)
			throw new InvalidSettingsException("Classifiers are not set!");
		PortObjectSpec imageSpec;
		imageSpec = new ImagePortObjectSpec(SvgCell.TYPE);
//		return new PortObjectSpec[]{imageSpec};
        if (generateImage()) {
        	return new PortObjectSpec[]{new ImagePortObjectSpec(SvgCell.TYPE)};
        } else {
        	return new PortObjectSpec[]{InactiveBranchPortObjectSpec.INSTANCE};
        }
	}

	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		table = (BufferedDataTable)inObjects[0];
		final var dts = table.getDataTableSpec();
		String[] sorting_columns = {m_settings.t_classifier, m_settings.time_classifier};
        final var sorter = new BufferedDataTableSorter(table, DefaultTableMinerNodeModel.toRowComparator(dts, sorting_columns));
        sorter.setSortInMemory(false);
        final BufferedDataTable sortedTable = sorter.sort(exec); 
        table = sortedTable;
		TraceVariantVisViewRepresentation representation = getViewRepresentation();
		
		String[] columns = table.getDataTableSpec().getColumnNames();
		String [] data = new String[columns.length+2];
		data[0] = Long.toString(table.size());
		data[1] = Long.toString(columns.length);
		for (int i=0; i<columns.length; i++) {
			data[i+2] = columns[i];
		}
		representation.setData(data);
		
		TraceVariantRepresentation varinats = new TraceVariantRepresentation(table, m_settings.t_classifier, m_settings.e_classifier);
		representation.setVariants(varinats);
	}
	
	@Override
	protected boolean generateImage() {
//		return true;
        return generate_image;
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
	protected void saveSettingsTo(NodeSettingsWO settings) {
//		settings.addString(KEY_TRACE_CLASSIFIER, m_settings.t_classifier);
//		settings.addString(KEY_EVENT_CLASSIFIER, m_settings.e_classifier);
//		settings.addBoolean("generate_image", generate_image);		
		if (m_settings != null) {
			NodeParametersUtil.saveSettings(m_settingsClass, m_settings, settings);
        }
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
//		m_settings.t_classifier = settings.getString(KEY_TRACE_CLASSIFIER);
//		m_settings.e_classifier = settings.getString(KEY_EVENT_CLASSIFIER);
//		generate_image = settings.getBoolean("generate_image", false);	
		m_settings = NodeParametersUtil.loadSettings(settings, m_settingsClass);
	}

	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {table};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		table = (BufferedDataTable) portObjects[0];
	}

	
	public String getEventClassifier() {
		return m_settings.e_classifier;		
	}
	
	
	public String getTraceClassifier() {
		return m_settings.t_classifier;		
	}
	
	public void setTraceClassifier(String c) {
		m_settings.t_classifier = c;
	}


	public void setEventClassifier(String c) {
		m_settings.e_classifier = c;
	}
	
	
}