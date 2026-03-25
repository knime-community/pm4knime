package org.pm4knime.node.discovery.hybridminer;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.node.DefaultModel;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.CausalGraphPortObjectSpec;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.extendedhybridminer.algorithms.cg2hpn.CGToHybridPN;
import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.hybridpetrinet.FitnessType;
import org.processmining.extendedhybridminer.plugins.HybridPNMinerPlugin;
import org.processmining.extendedhybridminer.plugins.HybridPNMinerSettings;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

public class HybridMinerNodeModel {

    private final NodeLogger logger = NodeLogger.getLogger(HybridMinerNodeModel.class);

    private HybridMinerNodeSettings m_settings;
    protected AbstractJSONPortObject hpnPO;
    protected CausalGraphPortObject cgPO;

    protected HybridMinerNodeModel(final Class<HybridMinerNodeSettings> modelSettingsClass) {
    }

    public static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o,
        final HybridMinerNodeModel model) throws InvalidSettingsException {

        model.m_settings = i.getParameters();

        if (i.getInPortSpec(0) == null) {
            o.setOutSpec(0, null);
            return;
        }

        if (!(i.getInPortSpec(0) instanceof CausalGraphPortObjectSpec spec)) {
            throw new InvalidSettingsException("Input is not a causal graph!");
        }

        o.setOutSpec(0, model.configureOutSpec(spec)[0]);
    }

    public static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o,
        final HybridMinerNodeModel model) {

        try {
            model.m_settings = i.getParameters();
            model.cgPO = (CausalGraphPortObject)i.getInPortObject(0);
            model.hpnPO = model.mine(model.cgPO.getCG(), i.getExecutionContext());

            o.setOutData(0, model.hpnPO);
            o.setInternalData(model.hpnPO);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected AbstractJSONPortObject mine(final ExtendedCausalGraph cg, final ExecutionContext exec)
        throws Exception {

        logger.info("Begin: Hybrid Petri Net Miner");

        final PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
            .getFutureResultAwarePluginContext(HybridPNMinerPlugin.class);
        HybridPNMinerSettings settings = getConfiguration();
        ExtendedHybridPetrinet pn = CGToHybridPN.fuzzyCGToFuzzyPN(cg, settings);
        pn = addMarkings(pluginContext, pn);
        final HybridPetriNetPortObject pnPO = new HybridPetriNetPortObject(pn);
        logger.info("End: Hybrid Petri Net miner");
        return pnPO;
    }

    private ExtendedHybridPetrinet addMarkings(final PluginContext context, final ExtendedHybridPetrinet pn) {
        final Place startPlace = pn.getPlace("start");
        final Marking im = new Marking();
        im.add(startPlace);
        final Place endPlace = pn.getPlace("end");
        final Marking fm = new Marking();
        fm.add(endPlace);
        context.getProvidedObjectManager().createProvidedObject(
            "Initial marking for " + pn.getLabel(),
            im, Marking.class, context);
        context.addConnection(new InitialMarkingConnection(pn, im));
        context.getProvidedObjectManager().createProvidedObject(
            "Final marking for " + pn.getLabel(),
            fm, Marking.class, context);
        context.addConnection(new FinalMarkingConnection(pn, fm));
        return pn;
    }

    HybridPNMinerSettings getConfiguration() {
        final HybridPNMinerSettings settings = new HybridPNMinerSettings();
        settings.setPlaceEvalThreshold(m_settings.t_fitness);
        try {
            settings.setFitnessType(getFitnessType());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return settings;
    }

    public FitnessType getFitnessType() throws Exception {
        if (m_settings.type_fitness.equals(HybridMinerNodeSettings.FitnessType.GLOBAL)) {
            return FitnessType.GLOBAL;
        } else if (m_settings.type_fitness.equals(HybridMinerNodeSettings.FitnessType.LOCAL)) {
            return FitnessType.LOCAL;
        } else {
            throw new Exception("Invalid place evaluation method: " + m_settings.type_fitness);
        }
    }

    protected PortObjectSpec[] configureOutSpec(final CausalGraphPortObjectSpec logSpec) {
        return new PortObjectSpec[]{new HybridPetriNetPortObjectSpec()};
    }
}
