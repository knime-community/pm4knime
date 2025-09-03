package org.pm4knime.util.defaultnode;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogUtil;

@SuppressWarnings("rawtypes")
public abstract class DefaultMinerNodeModel extends DefaultNodeModel {
	
	protected DefaultMinerNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}

	public static final String CFG_KEY_CLASSIFIER = "Event Classifier";
	public static final String CFG_KEY_CLASSIFIER_SET = "Event Classifier Set";
	

	protected SettingsModelString m_classifier =  new SettingsModelString(CFG_KEY_CLASSIFIER, "");
	SettingsModelStringArray classifierSet = new SettingsModelStringArray(CFG_KEY_CLASSIFIER_SET, 
			new String[] {""}) ;
	
	protected XLogPortObject logPO = null;
	

	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		logPO = (XLogPortObject) inObjects[0];
		

    	checkCanceled(null, exec);
		PortObject pmPO = mine(logPO.getLog(), exec);
		checkCanceled(null, exec);
		return new PortObject[] { pmPO};
	}
	

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(XLogPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Event Log!");
		
		if(m_classifier.getStringValue().isEmpty())
			throw new InvalidSettingsException("Classifier is not set");
		
		
		XLogPortObjectSpec logSpec = (XLogPortObjectSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	protected abstract PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) ;
	
	
	protected abstract PortObject mine(XLog log, final ExecutionContext exec) throws Exception; 

	
	public XEventClassifier getEventClassifier() {
		XLog log = logPO.getLog();
		return XLogUtil.getEventClassifier(log, m_classifier.getStringValue());
		
	}
	
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_classifier.saveSettingsTo(settings);
		classifierSet.saveSettingsTo(settings);
		saveSpecificSettingsTo(settings);
	}
	
	protected abstract void saveSpecificSettingsTo(NodeSettingsWO settings);

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		m_classifier.validateSettings(settings);
		validateSpecificSettings(settings);
	}
	
	protected abstract void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException;
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_classifier.loadSettingsFrom(settings);
		classifierSet.loadSettingsFrom(settings);
		
		loadSpecificValidatedSettingsFrom(settings);
	}

	protected abstract void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException;
	
	
	public XLogPortObject getXLogPO() {
    	return logPO; 
    }
}
