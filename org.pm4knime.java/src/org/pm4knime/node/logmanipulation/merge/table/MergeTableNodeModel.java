package org.pm4knime.node.logmanipulation.merge.table;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
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



@SuppressWarnings({"restriction" })
public class MergeTableNodeModel extends NodeModel {
	
	private static final NodeLogger logger = NodeLogger.getLogger(MergeTableNodeModel.class);
	
	protected MergeTableNodeSettings m_settings = new MergeTableNodeSettings();
    
	
	protected MergeTableNodeModel() {
        super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });    
        
    }

    
    @Override
    protected final PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return configure(inSpecs, m_settings);
    }

    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
 
        return (DataTableSpec[]) configure(inSpecs, m_settings);
    }
    
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs, final MergeTableNodeSettings modelSettings) throws InvalidSettingsException {

		m_settings = modelSettings;
		
		if(!inSpecs[0].getClass().equals(DataTableSpec.class)) 
    		throw new InvalidSettingsException("First input is not a valid Table!");
    	
    	if(!inSpecs[1].getClass().equals(DataTableSpec.class)) 
    		throw new InvalidSettingsException("Second input is not a valid Table!");
    	
    	if(modelSettings.t_classifier_0 == null || modelSettings.t_classifier_1 == null)
			throw new InvalidSettingsException("Classifiers are not set! Please open the dialog and configure the node!");
   
        DataTableSpec logSpec = MergeTableUtil.createSpec((DataTableSpec) inSpecs[0], (DataTableSpec) inSpecs[1], "", "");
		
        return new PortObjectSpec[]{logSpec};
	}

   
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin to merge event tables");
    	BufferedDataTable log0 = ((BufferedDataTable) inData[0]);
		BufferedDataTable log1 = ((BufferedDataTable) inData[1]);
		
		BufferedDataTable mlog = null;
    	if(m_settings.m_strategy.equals(MergeTableNodeSettings.CFG_TRACE_STRATEGY.get(0))) {
			mlog = MergeTableUtil.mergeTablesSeparate(log0, log1, m_settings.t_classifier_0, m_settings.t_classifier_1, exec);
		} else if(m_settings.m_strategy.equals(MergeTableNodeSettings.CFG_TRACE_STRATEGY.get(1))) {
			mlog = MergeTableUtil.mergeLogsIgnoreTrace(log0, log1, m_settings.t_classifier_0, m_settings.t_classifier_1, exec);
			
		} else if(m_settings.m_strategy.equals(MergeTableNodeSettings.CFG_TRACE_STRATEGY.get(2))) {
			mlog = MergeTableUtil.mergeLogsMergeTraces(log0, log1, m_settings.t_classifier_0, m_settings.t_classifier_1, exec);
		}else {
			System.out.println("Invalid strategy!");
		}
		
    	logger.info("End to merge event tables");
        return new BufferedDataTable[]{mlog};
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
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_settings != null) {
        	NodeParametersUtil.saveSettings(MergeTableNodeSettings.class, m_settings, settings);
        }
    }


    @Override
    protected final void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings = NodeParametersUtil.loadSettings(settings, MergeTableNodeSettings.class);
    }


	@Override
	protected void reset() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}



}


