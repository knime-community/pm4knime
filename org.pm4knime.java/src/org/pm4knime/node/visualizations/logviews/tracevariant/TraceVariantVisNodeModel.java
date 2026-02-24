package org.pm4knime.node.visualizations.logviews.tracevariant;

import java.util.*;

import org.knime.core.data.*;
import org.knime.core.data.def.*;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.*;
import org.knime.core.node.port.*;
import org.knime.core.node.web.ValidationError;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.pm4knime.util.defaultnode.TraceVariant;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;

@SuppressWarnings("restriction")
public class TraceVariantVisNodeModel
		extends AbstractSVGWizardNodeModel<TraceVariantVisViewRepresentation, TraceVariantVisViewValue>
		implements PortObjectHolder {

	private static final PortType[] IN_TYPES = { BufferedDataTable.TYPE };
	private static final PortType[] OUT_TYPES = { BufferedDataTable.TYPE, BufferedDataTable.TYPE };

	protected BufferedDataTable table;
	protected TraceVariantRepresentation m_variants;

	private Map<String, String> traceToVariant;

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
	protected boolean generateImage() {
		return false;
	}

	@Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	@Override
	public ValidationError validateViewValue(TraceVariantVisViewValue v) {
		return null;
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (!(inSpecs[0] instanceof DataTableSpec))
			throw new InvalidSettingsException("Input is not a valid Table!");
		if (m_settings.e_classifier == null || m_settings.t_classifier == null)
			throw new InvalidSettingsException("Classifiers are not set!");

		return new PortObjectSpec[] { createVariantTableSpec(), createLogWithVariantSpec((DataTableSpec) inSpecs[0]) };
	}


	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		table = (BufferedDataTable) inObjects[0];

		final var dts = table.getDataTableSpec();
		final var sorter = new BufferedDataTableSorter(table, DefaultTableMinerNodeModel.toRowComparator(dts,
				new String[] { m_settings.t_classifier, m_settings.time_classifier }));
		sorter.setSortInMemory(false);
		table = sorter.sort(exec);

		m_variants = new TraceVariantRepresentation(table, m_settings.t_classifier, m_settings.e_classifier);

		TraceVariantVisViewRepresentation representation = getViewRepresentation();

		String[] columns = table.getDataTableSpec().getColumnNames();
		String[] data = new String[columns.length + 2];
		data[0] = Long.toString(table.size());
		data[1] = Long.toString(columns.length);
		for (int i = 0; i < columns.length; i++) {
			data[i + 2] = columns[i];
		}
		representation.setData(data);
		representation.setVariants(m_variants);

		traceToVariant = buildTraceToVariantMapping();
	}

	
	private Map<String, String> buildTraceToVariantMapping() {
		List<TraceVariant> variants = m_variants.getVariants();
		Map<String, String> sequenceToVariantId = new HashMap<>(variants.size() * 2);

		for (int i = 0; i < variants.size(); i++) {
			String seq = String.join(" → ", variants.get(i).getActivities());
			sequenceToVariantId.put(seq, "Variant_" + (i + 1));
		}

		final DataTableSpec spec = table.getDataTableSpec();
		final int traceColIdx = spec.findColumnIndex(m_settings.t_classifier);
		final int eventColIdx = spec.findColumnIndex(m_settings.e_classifier);

		Map<String, List<String>> traceActivities = new LinkedHashMap<>();
		for (DataRow row : table) {
			traceActivities.computeIfAbsent(row.getCell(traceColIdx).toString(), k -> new ArrayList<>())
					.add(row.getCell(eventColIdx).toString());
		}

		Map<String, String> result = new HashMap<>(traceActivities.size() * 2);
		traceActivities.forEach((traceId, activities) -> {
			String seq = String.join(" → ", activities);
			String variantId = sequenceToVariantId.getOrDefault(seq, "UNKNOWN");
			result.put(traceId, variantId);
		});
		return result;
	}


	@Override
	protected PortObject[] performExecuteCreatePortObjects(PortObject svgImageFromView, PortObject[] inObjects,
			ExecutionContext exec) throws Exception {

		return new PortObject[] { createVariantSummaryTable(exec), createLogWithVariantTable(exec) };
	}

	
	private BufferedDataTable createVariantSummaryTable(ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(createVariantTableSpec());

		List<TraceVariant> variants = m_variants.getVariants();
		for (int i = 0; i < variants.size(); i++) {
			TraceVariant v = variants.get(i);
			String variantId = "Variant_" + (i + 1);
			String seq = String.join(" → ", v.getActivities());

			container.addRowToTable(new DefaultRow(RowKey.createRowKey((long) i), 
					new StringCell(variantId), new IntCell(v.getFrequency()), new StringCell(seq)));
		}

		container.close();
		return container.getTable();
	}

	private DataTableSpec createVariantTableSpec() {
		return new DataTableSpec("Variant Summary",
				new DataColumnSpec[] { new DataColumnSpecCreator("Variant ID", StringCell.TYPE).createSpec(),
						new DataColumnSpecCreator("Frequency", IntCell.TYPE).createSpec(),
						new DataColumnSpecCreator("Activity Sequence", StringCell.TYPE).createSpec() });
	}


	private BufferedDataTable createLogWithVariantTable(ExecutionContext exec) {
		DataTableSpec newSpec = createLogWithVariantSpec(table.getDataTableSpec());
		BufferedDataContainer container = exec.createDataContainer(newSpec);

		final int traceColIdx = table.getDataTableSpec().findColumnIndex(m_settings.t_classifier);

		for (DataRow row : table) {
			String variantId = traceToVariant.getOrDefault(row.getCell(traceColIdx).toString(), "UNKNOWN");

			List<DataCell> cells = new ArrayList<>(row.getNumCells() + 1);
			row.forEach(cells::add);
			cells.add(new StringCell(variantId));

			container.addRowToTable(new DefaultRow(row.getKey(), cells));
		}

		container.close();
		return container.getTable();
	}

	private DataTableSpec createLogWithVariantSpec(DataTableSpec originalSpec) {
		DataColumnSpec variantCol = new DataColumnSpecCreator("Variant ID", StringCell.TYPE).createSpec();
		DataColumnSpec[] allCols = new DataColumnSpec[originalSpec.getNumColumns() + 1];
		for (int i = 0; i < originalSpec.getNumColumns(); i++) {
			allCols[i] = originalSpec.getColumnSpec(i);
		}
		allCols[allCols.length - 1] = variantCol;
		return new DataTableSpec("Event Log with Variant ID", allCols);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		NodeParametersUtil.saveSettings(m_settingsClass, m_settings, settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_settings = NodeParametersUtil.loadSettings(settings, m_settingsClass);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
	}


	@Override
	public PortObject[] getInternalPortObjects() {
		return new PortObject[] { table };
	}

	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		table = (BufferedDataTable) portObjects[0];
	}

	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}
}