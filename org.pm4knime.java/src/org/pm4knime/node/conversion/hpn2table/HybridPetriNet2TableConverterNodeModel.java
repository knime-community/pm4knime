package org.pm4knime.node.conversion.hpn2table;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;


@SuppressWarnings("restriction")
public class HybridPetriNet2TableConverterNodeModel extends NodeModel {

	private static final NodeLogger logger = NodeLogger.getLogger(HybridPetriNet2TableConverterNodeModel.class);

	RowKey DEFAULT_ROWKEY = RowKey.createRowKey((long)0);
	
	String DEFAULT_COLUMN_LABLE = "Hybrid Petri Net";
	
	static String CFG_TABLE_NAME = "Converted Data Table from Hybrid Petri Net";

	private HybridPetriNetPortObjectSpec m_inSpec;

	protected HybridPetriNet2TableConverterNodeSettings m_settings = new HybridPetriNet2TableConverterNodeSettings();

	private final Class<HybridPetriNet2TableConverterNodeSettings> m_settingsClass;

	protected HybridPetriNet2TableConverterNodeModel(Class<HybridPetriNet2TableConverterNodeSettings> modelSettingsClass) {
		super( new PortType[]{HybridPetriNetPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
		m_settingsClass = modelSettingsClass;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected BufferedDataTable[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		logger.info("Start : Convert Hybrid Petri Net to DataTable" );
		HybridPetriNetPortObject pnPortObject = null ;
		for(PortObject obj: inData)
			if(obj instanceof HybridPetriNetPortObject) {
				pnPortObject = (HybridPetriNetPortObject)obj;
				break;
			}

		ExtendedHybridPetrinet anet = pnPortObject.getPN();

		DataTableSpec outSpec = createSpec();

		BufferedDataContainer bufCon = exec.createDataContainer(outSpec);

		RowKey rowKey;
		String rowKeyValue = m_settings.m_row_identifier;
		if (rowKeyValue == null || rowKeyValue.trim().isEmpty()) {
			rowKey = DEFAULT_ROWKEY;
		} else {
			rowKey = new RowKey(rowKeyValue);
		}


		DataRow eventRow = new DefaultRow(rowKey, new HybridPetriNetCell(anet));

		bufCon.addRowToTable(eventRow);

		bufCon.close();
		logger.info("End : Convert Hybrid Petri Net to DataTable" );
		return new BufferedDataTable[]{bufCon.getTable()};      

	}

	private DataTableSpec createSpec() {

		List<String> attrNames = new ArrayList<String>();
		List<DataType> attrTypes = new ArrayList<DataType>();

		attrNames.add(m_settings.m_column_name);

		attrTypes.add(DataType.getType(HybridPetriNetCell.class));

		DataTableSpec outSpec = new DataTableSpec(CFG_TABLE_NAME, 
				attrNames.toArray(new String[0]), attrTypes.toArray(new DataType[0]));

		return outSpec;

	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		// TODO: create a new DataTable there
		HybridPetriNetPortObjectSpec spec = (HybridPetriNetPortObjectSpec) inSpecs[0];

		if(!spec.getClass().equals(HybridPetriNetPortObjectSpec.class)) 
			throw new InvalidSettingsException("Input is not a valid Hybrid Petri Net!");

		m_inSpec = spec;

		String colName = m_settings.m_column_name;
		if (colName != null && colName.trim().isEmpty()) {
			throw new InvalidSettingsException("Please specify a column name.");
		}

		return new PortObjectSpec[]{null};
	}


	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		if (m_settings != null) {
			NodeParametersUtil.saveSettings(m_settingsClass, m_settings, settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_settings = NodeParametersUtil.loadSettings(settings, m_settingsClass);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}