package org.pm4knime.node.conversion.pn2table;

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
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;



@SuppressWarnings("restriction")
public class PetriNet2TableConverterNodeModel extends NodeModel {

	private static final NodeLogger logger = NodeLogger.getLogger(PetriNet2TableConverterNodeModel.class);

	protected PetriNet2TableConverterNodeSettings m_settings = new PetriNet2TableConverterNodeSettings();
	private final Class<PetriNet2TableConverterNodeSettings> m_settingsClass;

	RowKey DEFAULT_ROWKEY = RowKey.createRowKey((long)0);
	
	String DEFAULT_COLUMN_LABLE = "Petri Net";
	
	static String CFG_TABLE_NAME = "Converted Data Table from Petri Net";

	private PetriNetPortObjectSpec m_inSpec;
	//	ImageToTableNodeFactory ifac;
	//	ImageToTableNodeModel iMod;

	protected PetriNet2TableConverterNodeModel(Class<PetriNet2TableConverterNodeSettings> modelSettingsClass) {
		super( new PortType[]{PetriNetPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
		m_settingsClass = modelSettingsClass;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected BufferedDataTable[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		logger.info("Start : Convert Petri Net to DataTable" );
		PetriNetPortObject pnPortObject = null ;
		for(PortObject obj: inData)
			if(obj instanceof PetriNetPortObject) {
				pnPortObject = (PetriNetPortObject)obj;
				break;
			}

		AcceptingPetriNet anet = pnPortObject.getANet();

		DataTableSpec outSpec = createSpec();

		BufferedDataContainer bufCon = exec.createDataContainer(outSpec);

		RowKey rowKey;
		//String rowKey;
		String rowKeyValue = m_settings.m_row_identifier;
		if (rowKeyValue == null || rowKeyValue.trim().isEmpty()) {
			rowKey = DEFAULT_ROWKEY;
		} else {
			rowKey = new RowKey(rowKeyValue);
		}


		DataRow eventRow = new DefaultRow(rowKey, new PetriNetCell(anet));;

		bufCon.addRowToTable(eventRow);

		bufCon.close();
		logger.info("End : Convert Petri Net to DataTable" );
		return new BufferedDataTable[]{bufCon.getTable()};      

	}

	private DataTableSpec createSpec() {

		List<String> attrNames = new ArrayList<String>();
		List<DataType> attrTypes = new ArrayList<DataType>();

		attrNames.add(m_settings.m_column_name);

		attrTypes.add(DataType.getType(PetriNetCell.class));

		DataTableSpec outSpec = new DataTableSpec(CFG_TABLE_NAME, 
				attrNames.toArray(new String[0]), attrTypes.toArray(new DataType[0]));

		return outSpec;

	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		// TODO: create a new DataTable there
		PetriNetPortObjectSpec spec = (PetriNetPortObjectSpec) inSpecs[0];

		if(!spec.getClass().equals(PetriNetPortObjectSpec.class)) 
			throw new InvalidSettingsException("Input is not a valid Petri Net!");

		//    	if( spec.getGTraceAttrMap().isEmpty()|| spec.getClassifiersMap().isEmpty()) {
		//    		throw new InvalidSettingsException("Log Spec Object is Empty. Probably because the reader node got reset");
		//    	}

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
		validateSettings(NodeParametersUtil.loadSettings(settings, m_settingsClass));
	}

	private void validateSettings(PetriNet2TableConverterNodeSettings settings) {
		// TODO Auto-generated method stub	

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