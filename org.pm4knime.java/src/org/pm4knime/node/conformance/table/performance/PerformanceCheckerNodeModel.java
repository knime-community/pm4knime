package org.pm4knime.node.conformance.table.performance;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PNManifestFlattenerTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PNManifestReplayerParameterTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PerfCounterTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ReliablePerfCounterTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.TableEventLog;
import org.pm4knime.node.conformance.replayer.table.helper.DefaultPNReplayerTableUtil.ParameterGenerator;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestEvClassPatternTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestFactoryTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestWithSerializerTable;
//import org.pm4knime.portobject.ManifestWithSerializer;
import org.pm4knime.portobject.RepResultPortObjectTable;
import org.pm4knime.portobject.RepResultPortObjectSpecTable;
import org.pm4knime.settingsmodel.SMPerformanceParameter;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;


@SuppressWarnings({ "rawtypes", "restriction" })
public class PerformanceCheckerNodeModel extends DefaultNodeModel implements PortObjectHolder {
	private static final NodeLogger logger = NodeLogger.getLogger(PerformanceCheckerNodeModel.class);

	private static final String CFG_MC_MANIFEST = "Model Content for Manifest";
	
	protected PerformanceCheckerNodeSettings m_settings = new PerformanceCheckerNodeSettings();
    private final Class<PerformanceCheckerNodeSettings> m_settingsClass;

	// we create a similar nodeSetting like Conformance Checking?
	SMPerformanceParameter m_parameter;
	RepResultPortObjectSpecTable m_rSpec;
	private ManifestTable mResult;
	private PerfCounterTable counter;
	RepResultPortObjectTable repResultPO;

	private File file;

	private ExecutionMonitor exe;

	

//	protected PerformanceCheckerNodeModel() {
//
//		// TODO: Specify the amount of input and output ports needed.
//		super(new PortType[] { RepResultPortObjectTable.TYPE },
//				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE });
//		m_parameter = new SMPerformanceParameter("Performance Parameter");
//	}


	public PerformanceCheckerNodeModel(Class<PerformanceCheckerNodeSettings> modelSettingsClass) {
		// TODO: Specify the amount of input and output ports needed.
		super(new PortType[] { RepResultPortObjectTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		m_parameter = new SMPerformanceParameter("Performance Parameter");
		m_settingsClass = modelSettingsClass;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		// TODO: Return a BufferedDataTable for each output port
		logger.info("Start: ManifestReplayer Performance Checking");
		
		repResultPO = (RepResultPortObjectTable) inData[0];
		TableEventLog log = repResultPO.getLog();
		AcceptingPetriNet anet = repResultPO.getNet();

		PNRepResult repResult = repResultPO.getRepResult();
		
		int[] default_costs = repResultPO.getDefaultMoveCosts();
		Map<String, Integer>[] cost_maps = repResultPO.getMoveCostMaps();

		PNManifestReplayerParameterTable manifestParameters = ParameterGenerator.getPerfParameter(log, anet, default_costs, cost_maps);
		PNManifestFlattenerTable flattener = new PNManifestFlattenerTable(anet.getNet(), manifestParameters);
		
// check cancellation of node before sync
    	checkCanceled(null, exec);
		sync(repResult, flattener, exec);

// check cancellation of node before replaying
		checkCanceled(null, exec);
		mResult = ManifestFactoryTable.construct(anet.getNet(), anet.getInitialMarking(),
				anet.getFinalMarkings().toArray(new Marking[0]), log, flattener, repResult, manifestParameters.getMapping());
		//		mResult = ManifestFactory.construct(flattener.getNet(), flattener.getInitMarking(),
//				flattener.getFinalMarkings(), log, flattener, repResult, manifestParameters.getMapping());
		checkCanceled(null, exec);
		// global statistics information. It includes all the performance info, the
		// whole process
		// we need one view to show the result here
		if (m_parameter.isMWithSynMove().getBooleanValue()) {
			counter = new ReliablePerfCounterTable();
		} else
			counter = new PerfCounterTable();
		PerfCheckerInfoAssistantTable infoAssistant = new PerfCheckerInfoAssistantTable(m_parameter, mResult, counter);

		DataTableSpec gSpec = createGlobalStatsTableSpec();
		BufferedDataContainer gBuf = exec.createDataContainer(gSpec);
		// here to fill the values from result to gBuf
		infoAssistant.fillGlobalData(gBuf);

		// create one for transition, one for place there
		DataTableSpec tSpec = createElemenentStatsTableSpec("Transition");
		BufferedDataContainer tBuf = exec.createDataContainer(tSpec);
		infoAssistant.fillTransitionData(tBuf, anet.getNet().getTransitions());

		DataTableSpec pSpec = createElemenentStatsTableSpec("Place");
		BufferedDataContainer pBuf = exec.createDataContainer(pSpec);
		infoAssistant.fillPlaceData(pBuf, anet.getNet().getPlaces());

		gBuf.close();
		tBuf.close();
		pBuf.close();
// check cancellation of node after replaying
		
		logger.info("End: ManifestReplayer Performance Evaluation");
		return new PortObject[] { gBuf.getTable(), tBuf.getTable(), pBuf.getTable() };
	}

	private void sync(PNRepResult repResult, PNManifestFlattenerTable flattener, ExecutionContext exec) throws CanceledExecutionException {
		// TODO make the transition in the same transition ids here
		// set a map here to record the connection?? Or, we can reload the nodes by
		// making the transition the same
		
		for (SyncReplayResult alignment : repResult) {
			checkCanceled(null, exec);
			
			List<Object> nodeInstances = alignment.getNodeInstance();
			for (int idx = 0; idx < nodeInstances.size(); idx++) {
				Object node = nodeInstances.get(idx);
				if (node instanceof Transition) {
					Transition tInResult = (Transition) node;
					// here make the wrong match for silent transition only with label
					// to include silent transition, we need to have one order of the transition..
					// because it is from the accpeting petri net, so there should be some order to match it 
					// it can't work out because flattener net changes its structure by adding more transitions
					// the thing is to convert the performance calculation information there. 
					// so go to further about the structure there, no need to repeat the use of them.
					
					int tIdx = flattener.getOrigTrans2Int().get(tInResult);
					Transition tValue = flattener.getFlatTransArr()[tIdx];
					
					nodeInstances.set(idx, tValue);
				}
			}
		}

	}

	public ManifestTable getMainfestResult() {
		return mResult;
	}

	public SMPerformanceParameter getMParameter() {
		return m_parameter;
	}

	/**
	 * this method create a table for the element statistics info. It can be used
	 * for transitions, but also for the places. But how to get this?? We should
	 * have columnClassifier
	 * 
	 * From the parameters, we could create a table spec from it
	 * 
	 * @return
	 */
	private DataTableSpec createElemenentStatsTableSpec(String itemColName) {
		// here we need to change the table spec according to the places
		String[] columnNames = { itemColName, "Property", "Min.", "Max.", "Avg.", "Std. Dev", "Freq." };
		DataType[] columnTypes = { StringCell.TYPE, StringCell.TYPE, StringCell.TYPE, StringCell.TYPE, StringCell.TYPE,
				StringCell.TYPE, StringCell.TYPE };
		DataTableSpec tSpec = new DataTableSpec(itemColName + " Performance Table", columnNames, columnTypes);
		return tSpec;
	}

	/**
	 * there is one global table for this, so
	 */
	private DataTableSpec createGlobalStatsTableSpec() {
		String[] columnNames = { "Case Property", "Value" };
		DataType[] columnTypes = { StringCell.TYPE, StringCell.TYPE };
		DataTableSpec tSpec = new DataTableSpec("Global Performance Statistics Table", columnNames, columnTypes);
		return tSpec;
	}

	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(RepResultPortObjectSpecTable.class))
			throw new InvalidSettingsException("Input is not a valid replay result!");
		
		m_rSpec = (RepResultPortObjectSpecTable) inSpecs[0];
		return new PortObjectSpec[] { null, null, null };

	}


	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO deserialize the manifest and other related data for view
		file = nodeInternDir;
		exe = exec;	
	}
	

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		File manifestDir = new File(nodeInternDir, CFG_MC_MANIFEST);
		manifestDir.mkdirs();
		ManifestWithSerializerTable.saveTo((ManifestEvClassPatternTable) mResult, manifestDir , exec);
		
	}
	

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
	
	}
	
	public RepResultPortObjectTable getRepResultPO() {
		// TODO Auto-generated method stub
		return repResultPO;
	}
	
	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		repResultPO = (RepResultPortObjectTable)portObjects[0];
		try {
			File manifestDir = new File(file, CFG_MC_MANIFEST);
			TableEventLog log = repResultPO.getLog();
			try {
				mResult = ManifestWithSerializerTable.loadFrom(manifestDir, exe, log);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CanceledExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {repResultPO};
	
	}
	
      
	@Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
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
		
	
}
