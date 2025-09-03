package org.pm4knime.node.discovery.inductiveminer.Table;

import java.time.Duration;
import java.time.Instant;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.BufferedTableIMLog;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIM;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIMInfrequent;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIMLifeCycle;
import org.processmining.processtree.ProcessTree;



public class InductiveMinerTableNodeModel extends DefaultTableMinerNodeModel<InductiveMinerTableNodeSettings> {
    
	private static final NodeLogger logger = NodeLogger.getLogger(InductiveMinerTableNodeModel.class);


		protected InductiveMinerTableNodeModel(Class<InductiveMinerTableNodeSettings> modelSettingsClass) {
			super( new PortType[]{BufferedDataTable.TYPE } , new PortType[] { ProcessTreePortObject.TYPE }, "Process Tree JS View", modelSettingsClass);
		}

		@Override
		protected PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) {
			ProcessTreePortObjectSpec ptSpec = new ProcessTreePortObjectSpec();
			return new PortObjectSpec[] { ptSpec };
		}
		

		@Override
		protected AbstractJSONPortObject mine(BufferedDataTable log, final ExecutionContext exec) throws Exception {
			logger.info("Begin: Inductive Miner");
			String activityClassifier = m_settings.e_classifier;
			IMLog imlog =  new BufferedTableIMLog(log, activityClassifier, m_settings.t_classifier);
			System.out.println("End of Generating Log");
			MiningParametersIM param =  createParameters();
			XEventClassifier classifi = new XEventAttributeClassifier(activityClassifier);
			param.setClassifier(classifi);
			Instant start = Instant.now();
			EfficientTree ptE = InductiveMinerPlugin.mineTree(imlog, param,  new Canceller() {
				public boolean isCancelled() {
					return false;
				}
			});
			
			Instant end = Instant.now();
			System.out.println(Duration.between(start, end).toMinutes());
			System.out.println("End of Inductive Miner");
			ProcessTree tree = EfficientTree2processTree.convert(ptE);

			ProcessTreePortObject treeObj = new ProcessTreePortObject(tree);
			logger.info("End:  Inductive Miner");
			return  treeObj;

		}

		private MiningParametersIM createParameters() throws InvalidSettingsException {
			MiningParametersIM param;

			if (m_settings.m_variant.equals(InductiveMinerTableNodeSettings.variantList.get(0)))
				param = new MiningParametersIM();
			else if (m_settings.m_variant.equals(InductiveMinerTableNodeSettings.variantList.get(1)))
				param = new MiningParametersIMInfrequent();
			else if (m_settings.m_variant.equals(InductiveMinerTableNodeSettings.variantList.get(2)))
				param = new MiningParametersIMInfrequent();
			else if (m_settings.m_variant.equals(InductiveMinerTableNodeSettings.variantList.get(3)))
				param = new MiningParametersIMLifeCycle();
			else
				throw new InvalidSettingsException("unknown inductive miner type ");
			param.setNoiseThreshold((float) m_settings.m_noise);
			return param;
		}

}
