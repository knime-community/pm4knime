package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.node.visualizations.jsgraphviz.util.WebUIJSViewNodeModel;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.DfgMsdPortObjectSpec;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.framework.packages.PackageManager.Canceller;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerWithoutLogPlugin;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.variants.MiningParametersIMWithoutLog;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "InductiveMinerDFGTable" node.
 *
 * @author
 */
public class InductiveMinerDFGTableNodeModel extends WebUIJSViewNodeModel<InductiveMinerDFGTableNodeSettings, JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {

	private static final NodeLogger logger = NodeLogger.getLogger(InductiveMinerDFGTableNodeModel.class);

	protected ProcessTreePortObject ptpo;
	protected DfgMsdPortObject dfgMsdPO;
	
	private InductiveMinerDFGTableNodeSettings m_settings = new InductiveMinerDFGTableNodeSettings();


	protected InductiveMinerDFGTableNodeModel(Class<InductiveMinerDFGTableNodeSettings> class1) {

		super(new PortType[] { DfgMsdPortObject.TYPE }, new PortType[] { ProcessTreePortObject.TYPE }, "Process Tree JS View", class1);
	}

	
	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{ptpo};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		logger.info("Begin:  Inductive miner Miner");

		dfgMsdPO = (DfgMsdPortObject) inObjects[0];

		DfgMsd dfmMsd = dfgMsdPO.getDfgMsd();

		MiningParametersIMWithoutLog params = new MiningParametersIMWithoutLog();
		params.setNoiseThreshold((float) m_settings.m_noiseThreshold);

		EfficientTree ptEff = InductiveMinerWithoutLogPlugin.mineTree(dfmMsd, new MiningParametersIMWithoutLog(),
				new Canceller() {
					public boolean isCancelled() {
						try {
							exec.checkCanceled();
						} catch (final CanceledExecutionException ce) {
							return true;
						}
						return false;
					}
				});

		ProcessTree tree = EfficientTree2processTree.convert(ptEff);

		ptpo = new ProcessTreePortObject(tree);
		logger.info("End:  Inductive Miner");

		JSGraphVizViewRepresentation representation = getViewRepresentation();

	    representation.setJSONString(ptpo.getJSON());

	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, InductiveMinerDFGTableNodeSettings modelSettings) throws InvalidSettingsException {

		m_settings = modelSettings;
		if (!inSpecs[0].getClass().equals(DfgMsdPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid DFG model!");

		DfgMsdPortObjectSpec dfgSpec = new DfgMsdPortObjectSpec();

		return new PortObjectSpec[]{new ProcessTreePortObjectSpec()};
	}
	
	@Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	
	@Override
    protected boolean generateImage() {
        return false;
    }
	
	
	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
	}
	
	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}
	
	
	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
	}


	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {dfgMsdPO};
	}

	public void setInternalPortObjects(PortObject[] portObjects) {
		dfgMsdPO = (DfgMsdPortObject) portObjects[0];
	}

}
