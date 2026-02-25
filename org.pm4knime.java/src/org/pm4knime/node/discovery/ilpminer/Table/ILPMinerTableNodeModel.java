package org.pm4knime.node.discovery.ilpminer.Table;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.node.discovery.ilpminer.Table.util.TableHybridILPMinerParametersImpl;
import org.pm4knime.node.discovery.ilpminer.Table.util.TableHybridILPMinerPlugin;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategy;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategyType;
import org.processmining.hybridilpminer.parameters.LPConstraintType;
import org.processmining.hybridilpminer.parameters.LPFilter;
import org.processmining.hybridilpminer.parameters.LPFilterType;
import org.processmining.hybridilpminer.parameters.LPObjectiveType;
import org.processmining.hybridilpminer.parameters.LPVariableType;
import org.processmining.hybridilpminer.parameters.NetClass;
import org.processmining.lpengines.interfaces.LPEngine.EngineType;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.List;
import java.util.Map;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;

/**
 * <code>NodeModel</code> for the "ILPMiner" node. 
 * This node is for ILPMiner based on Hybridilpminer in ProM. 
 * Input: 
 * 		event log
 * Output: 
 *   	Petri net (or causal graph?? generated from the values?? We might need it But at which sense?? )
 * Parameter: 
 * 		Normal Options:
 * 			-- event classifier
 * 			-- filter type and threshold :
 * 				  	LP-Filter : sequence-encoding filter, slack variable fiter, none
 * 			-- miner strategy
 * 		Advanced Options: 
 * 			-- LP Objective: unweighted values, weighted values/ relative absolute freq
 *  		-- LP Variable type: two variable per event, one variable per event
 *  		-- Discovery Strategy: mine a place per causal relation, a connection place between each pair
 *   
 * 		In default:
 * 			-- desired resulting net: Petri net
 * 			-- empty after completion
 * 			-- add sink place
 * 			-- use dot to previw graph
 * 
 * Steps:
 * 	
 * Modifications: 2020-02-12 
 *   Need to delete some options to avoid the flower model. 
 *      Casual_E_Vereek + its corresponding algorithms
 *      
 *  SLACK_VAR is slow but will be kept. 
 *   After this, we can merge the options in one Panel : Options. 
 * Notes: I wonder if I could put all the parameters into one big class and follow this strategy for other nodes, too. 
 * The reason for it is to reduce the codes in one nodes. Make it bettter to use this. Other reasons to use other steps for it.
 * All of them is in default mode. SO we can use it 
 * @author Kefang Ding
 */

/*
 * TODO:
 * 1. to replace all the event log to the table(BufferedDataTable)
 * 
 */
public class ILPMinerTableNodeModel extends DefaultTableMinerNodeModel<ILPMinerTableNodeSettings> {
	
	private static final NodeLogger logger = NodeLogger
            .getLogger(ILPMinerTableNodeModel.class);

	
    protected ILPMinerTableNodeModel(Class<ILPMinerTableNodeSettings> class1) {
    	super(new PortType[] {BufferedDataTable.TYPE}, new PortType[] {PetriNetPortObject.TYPE}, "Petri Net JS View", class1);
    }
    

	protected PortObjectSpec[] configureOutSpec(DataTableSpec tableSpec) {
		// TODO Auto-generated method stub
		PetriNetPortObjectSpec pnSpec = new PetriNetPortObjectSpec();
        return new PortObjectSpec[]{pnSpec};
	}
	
	@Override
	protected AbstractJSONPortObject mine(BufferedDataTable table, final ExecutionContext exec) throws Exception {
		
        logger.info("Start : ILPMiner " );
        
        final String startLabel = "ARTIFICIAL_START";
		final String endLabel = "ARTIFICIAL_END";
		
		TraceVariantRepresentation log = new TraceVariantRepresentation(table, m_settings.t_classifier, m_settings.e_classifier);
		TraceVariantRepresentation artifLog = TraceVariantRepresentation.addArtificialStartAndEnd(log.getNumberOfTraces(), log.getActivities(), log.getVariants(), startLabel, endLabel);
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
        // create the parameter
		TableHybridILPMinerParametersImpl param = new TableHybridILPMinerParametersImpl(context, artifLog);
		// here put some values from m_parameter to param
		updateParameter(param);
      
    	Object[] result = TableHybridILPMinerPlugin.discoverWithArtificialStartEnd(context, log, artifLog, param, exec);
        
    	// create the accepting Petri net and PortObject
    	AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1],  (Marking) result[2]);
        PetriNetPortObject pnPO = new PetriNetPortObject(anet);
        
    	logger.info("End : ILPMiner " );
        return pnPO;
		
		
	}


	private void updateParameter(TableHybridILPMinerParametersImpl param) {
		// set default values to param here, others we need to count it later
		param.setDiscoveryStrategy(new DiscoveryStrategy(DiscoveryStrategyType.valueOf(m_settings.m_ds)));
		param.setObjectiveType(LPObjectiveType.valueOf(m_settings.m_lpObj));
		param.setVariableType(LPVariableType.valueOf(m_settings.m_lpVar));
		// set the filter type
		LPFilter filter = new LPFilter(LPFilterType.valueOf(m_settings.m_filterType),
				m_settings.m_filterThreshold);
		param.setFilter(filter);
		
		// in default settings
		param.setNetClass(NetClass.PT_NET);
		param.getLPConstraintTypes().add(LPConstraintType.EMPTY_AFTER_COMPLETION);
		param.setFindSink(true); // add sink to model
		param.setEngineType(EngineType.LPSOLVE);
		param.setApplyStructuralRedundantPlaceRemoval(false);
		
	}
	

}

