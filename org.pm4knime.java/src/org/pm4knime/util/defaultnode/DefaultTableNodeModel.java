package org.pm4knime.util.defaultnode;

import java.io.File;
import java.io.IOException;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;




@SuppressWarnings("restriction")
public abstract class DefaultTableNodeModel<S extends DefaultTableNodeSettings> extends NodeModel {
	
	protected S m_settings;

    private final Class<S> m_settingsClass;
	
	protected DefaultTableNodeModel(PortType[] inPortTypes, PortType[] outPortTypes, final Class<S> modelSettingsClass) {
		super(inPortTypes, outPortTypes);
		m_settingsClass = modelSettingsClass;
		try {
			m_settings = m_settingsClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate settings class: " + m_settingsClass.getName(), e);
        }
	}
	
	
	@Override
    protected final PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {	
        return configure(inSpecs, m_settings);
    }

    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return (DataTableSpec[]) configure(inSpecs, m_settings);
    }
    
    
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs, final S modelSettings) throws InvalidSettingsException {

		m_settings = modelSettings;
		if (!inSpecs[0].getClass().equals(DataTableSpec.class))
			throw new InvalidSettingsException("Input is not a valid Table!");
		DataTableSpec logSpec = (DataTableSpec) inSpecs[0];
		if(modelSettings.e_classifier == null || modelSettings.t_classifier == null || modelSettings.time_classifier == null)
			throw new InvalidSettingsException("Classifiers are not set! Please open the dialog and configure the node!");
		return configureOutSpec(logSpec);
	}
        
    protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec);	
        
    @Override
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_settings != null) {
        	NodeParametersUtil.saveSettings(m_settingsClass, m_settings, settings);
        }
    }

    @Override
    protected final void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        validateSettings(NodeParametersUtil.loadSettings(settings, m_settingsClass));
    }

    protected void validateSettings(final S settings) throws InvalidSettingsException {
    }
    
    protected abstract void validateSpecificSettings(final NodeSettingsRO settings) throws InvalidSettingsException;

	@Override
    protected final void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings = NodeParametersUtil.loadSettings(settings, m_settingsClass);
        validateSpecificSettings(settings);
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
	protected void reset() {
		// TODO Auto-generated method stub
		
	}


}
