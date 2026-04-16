package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.node.DefaultModel;
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

public class InductiveMinerDFGTableNodeModel {

    private static final NodeLogger logger = NodeLogger.getLogger(InductiveMinerDFGTableNodeModel.class);

    protected ProcessTreePortObject ptpo;
    protected DfgMsdPortObject dfgMsdPO;
    private InductiveMinerDFGTableNodeSettings m_settings = new InductiveMinerDFGTableNodeSettings();

    protected InductiveMinerDFGTableNodeModel(final Class<InductiveMinerDFGTableNodeSettings> class1) {
    }

    public static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o,
        final InductiveMinerDFGTableNodeModel model) throws InvalidSettingsException {

        model.m_settings = i.getParameters();

        if (i.getInPortSpec(0) == null) {
            o.setOutSpec(0, null);
            return;
        }

        if (!(i.getInPortSpec(0) instanceof DfgMsdPortObjectSpec)) {
            throw new InvalidSettingsException("Input is not a valid DFG model!");
        }

        o.setOutSpec(0, new ProcessTreePortObjectSpec());
    }

    public static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o,
        final InductiveMinerDFGTableNodeModel model) {

        try {
            model.m_settings = i.getParameters();
            model.dfgMsdPO = (DfgMsdPortObject)i.getInPortObject(0);

            logger.info("Begin:  Inductive miner Miner");

            final DfgMsd dfmMsd = model.dfgMsdPO.getDfgMsd();
            final MiningParametersIMWithoutLog params = new MiningParametersIMWithoutLog();
            params.setNoiseThreshold((float)model.m_settings.m_noiseThreshold);

            final EfficientTree ptEff = InductiveMinerWithoutLogPlugin.mineTree(dfmMsd, params, new Canceller() {
                @Override
                public boolean isCancelled() {
                    try {
                        i.getExecutionContext().checkCanceled();
                    } catch (final CanceledExecutionException ce) {
                        return true;
                    }
                    return false;
                }
            });

            final ProcessTree tree = EfficientTree2processTree.convert(ptEff);
            model.ptpo = new ProcessTreePortObject(tree);

            logger.info("End:  Inductive Miner");

            o.setOutData(0, model.ptpo);
            o.setInternalData(model.ptpo);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected PortObjectSpec[] configureOutSpec() {
        return new PortObjectSpec[]{new ProcessTreePortObjectSpec()};
    }
}
