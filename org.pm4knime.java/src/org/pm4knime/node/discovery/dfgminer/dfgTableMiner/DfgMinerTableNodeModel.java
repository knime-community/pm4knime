package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings;
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.BufferedTableIMLog;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.DfgMsdPortObjectSpec;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.Log2DfgMsd;


public class DfgMinerTableNodeModel extends DefaultTableMinerNodeModel<DefaultTableMinerSettings> {
    
	private NodeLogger logger = NodeLogger.getLogger(DfgMinerTableNodeModel.class);

		
    protected DfgMinerTableNodeModel(Class<DefaultTableMinerSettings> modelSettingsClass) {
    
        // TODO: Specify the amount of input and output ports needed.
    	super( new PortType[]{BufferedDataTable.TYPE }, new PortType[] { DfgMsdPortObject.TYPE }, "DFG JS View", modelSettingsClass); 
    }

	@Override
	protected AbstractJSONPortObject mine(BufferedDataTable log, final ExecutionContext exec) throws Exception {
		logger.info("Begin:  DFM Miner");	
		exec.checkCanceled();
		BufferedTableIMLog imLog = new BufferedTableIMLog(logPO, m_settings.e_classifier, m_settings.t_classifier, exec);	
		exec.checkCanceled();
		DfgMsd dfgmsd = Log2DfgMsd.convert(imLog);
		exec.checkCanceled();
		logger.info("End:  DFM Miner");
		DfgMsdPortObject dfgMsdObj = new DfgMsdPortObject(dfgmsd);
		return dfgMsdObj;
	}


	@Override
	protected PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) {
		// TODO Auto-generated method stub
			return new PortObjectSpec[] { new DfgMsdPortObjectSpec() };			
	}

}

