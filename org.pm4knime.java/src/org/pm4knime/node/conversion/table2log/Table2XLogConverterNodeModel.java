package org.pm4knime.node.conversion.table2log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.SMTable2XLogConfig;


@SuppressWarnings("restriction")
public class Table2XLogConverterNodeModel extends NodeModel {
    
	private static final NodeLogger logger = NodeLogger.getLogger(Table2XLogConverterNodeModel.class);

	XLogPortObject logPO;
	
	protected Table2XLogConverterNodeSettings m_settings = new Table2XLogConverterNodeSettings();

    private final Class<Table2XLogConverterNodeSettings> m_settingsClass;


    public Table2XLogConverterNodeModel(Class<Table2XLogConverterNodeSettings> modelSettingsClass) {
       super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{XLogPortObject.TYPE});
       m_settingsClass = modelSettingsClass;
	}


    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start : Convert DataTable to Event Log" );

    	BufferedDataTable tData = (BufferedDataTable) inData[0];
    	
    	List<String> m_inclList = new ArrayList<String>();
    	m_inclList.add(m_settings.case_id);
    	boolean[] m_sortOrder = {true};
    	boolean m_missingToEnd = false;
    	boolean m_sortInMemory = false;
    	BufferedDataTableSorter sorter = new BufferedDataTableSorter(
    			tData, m_inclList, m_sortOrder, m_missingToEnd);
    	
        sorter.setSortInMemory(m_sortInMemory);
        BufferedDataTable sortedTable = sorter.sort(exec);

    	ToXLogConverter handler = new ToXLogConverter();

    	handler.setLogger(logger);
    	
    	handler.convertDataTable2Log(sortedTable, m_settings, exec);
    	XLog log = handler.getXLog();
    	
    	logPO = new XLogPortObject(log);
    	
    	logger.info("End : Convert DataTable to Event Log" );
        return new PortObject[]{logPO};
    }


	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
    	
    	if (inSpecs[0] == null) {
            return new PortObjectSpec[]{null};
        }

        if (!(inSpecs[0] instanceof DataTableSpec)) {
            throw new InvalidSettingsException("Input port must be connected to a data table.");
        }
        
    	
    	String tsName = m_settings.time_stamp;
    	DataTableSpec spec  = (DataTableSpec) inSpecs[0];
    	
    	if(!tsName.equals(SMTable2XLogConfig.CFG_NO_OPTION)&&!spec.getColumnSpec(tsName).getType().equals(LocalDateTimeCellFactory.TYPE) &&
    			!spec.getColumnSpec(tsName).getType().equals(ZonedDateTimeCellFactory.TYPE))
    		throw new InvalidSettingsException("The time stamp doesn't have the required format in LocalDateTime or ZonedDateTime");
    	    	
    	String[] all_columns = spec.getColumnNames();
		
		String[] traceColumns = m_settings.m_columnFilterTrace.filterFromFullSpec(spec);
		List<String> traceList = Arrays.asList(traceColumns);
		Set<String> traceSet = new HashSet<>(traceList);
		
		List<String> eventList = new ArrayList<>();
		for (String col : all_columns) {
		    if (!traceSet.contains(col)) {
		        eventList.add(col);
		    }
		}
    	
    	if(traceList.contains(m_settings.case_id)) 
    		if(eventList.contains(m_settings.event_class))
    			if(eventList.contains(m_settings.life_cycle)
    					|| m_settings.life_cycle.equals(SMTable2XLogConfig.CFG_NO_OPTION))
    				if(eventList.contains(m_settings.time_stamp) 
    						|| m_settings.time_stamp.equals(SMTable2XLogConfig.CFG_NO_OPTION))
    					return new PortObjectSpec[]{new XLogPortObjectSpec()};
   	
    	if(!traceList.contains(m_settings.case_id)) 
    		throw new InvalidSettingsException("Please ensure that Case Identifier is a trace attribute.");
    	if(!eventList.contains(m_settings.event_class))
    		throw new InvalidSettingsException("Please ensure that Event Identifier is an event attribute.");
    	if(!(eventList.contains(m_settings.life_cycle) 
    			|| m_settings.life_cycle.equals(SMTable2XLogConfig.CFG_NO_OPTION)))
    		throw new InvalidSettingsException("Please ensure that Life Cycle Identifier is either set to MISSING or is an event attribute.");
    	if(!(eventList.contains(m_settings.time_stamp) 
    			|| m_settings.time_stamp.equals(SMTable2XLogConfig.CFG_NO_OPTION)))
    		throw new InvalidSettingsException("Please ensure that Timestamp Identifier is either set to MISSING or is an event attribute.");
    	throw new InvalidSettingsException("Default error message");
    	
    }

    /**
     * {@inheritDoc}
     */
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
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_settings = NodeParametersUtil.loadSettings(settings, m_settingsClass);
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

	public XLogPortObject getLogPO() {
		// TODO Auto-generated method stub
		return logPO;
	}

}