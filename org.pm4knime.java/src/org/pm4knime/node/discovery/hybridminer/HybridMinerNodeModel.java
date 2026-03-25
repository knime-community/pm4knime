package org.pm4knime.node.discovery.hybridminer;

import org.deckfour.xes.classification.XEventClassifier;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.node.DefaultModel;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.node.visualizations.jsgraphviz.util.WebUIJSViewNodeModel;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.CausalGraphPortObjectSpec;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.extendedhybridminer.algorithms.cg2hpn.CGToHybridPN;
import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.hybridpetrinet.FitnessType;
import org.processmining.extendedhybridminer.plugins.HybridPNMinerPlugin;
import org.processmining.extendedhybridminer.plugins.HybridPNMinerSettings;


public class HybridMinerNodeModel extends WebUIJSViewNodeModel<HybridMinerNodeSettings, JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {
	
	private final NodeLogger logger = NodeLogger
            .getLogger(HybridMinerNodeModel.class);
	
	private HybridMinerNodeSettings m_settings;
		
	protected AbstractJSONPortObject hpnPO;
	protected CausalGraphPortObject cgPO;

	protected HybridMinerNodeModel(final Class<HybridMinerNodeSettings> modelSettingsClass) {
		super(new PortType[] { CausalGraphPortObject.TYPE }, 
        		new PortType[] { HybridPetriNetPortObject.TYPE }, "Hybrid Petri Net View", modelSettingsClass);
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
	
	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{hpnPO};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		cgPO = (CausalGraphPortObject) inObjects[0];
		hpnPO = mine(cgPO.getCG(), exec);
		
		final String dotstr;
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		representation.setJSONString(hpnPO.getJSON());

	}
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs, final HybridMinerNodeSettings modelSettings) throws InvalidSettingsException {

		m_settings = modelSettings;
		if (!inSpecs[0].getClass().equals(CausalGraphPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a causal graph!");		
		CausalGraphPortObjectSpec logSpec = (CausalGraphPortObjectSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	
	
	public XEventClassifier getEventClassifier() {
		ExtendedCausalGraph cg = cgPO.getCG();
		return cg.getSettings().getClassifier();	
	}
	
    
    protected AbstractJSONPortObject mine(ExtendedCausalGraph cg, final ExecutionContext exec) throws Exception{
    	logger.info("Begin: Hybrid Petri Net Miner");
    	
    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(HybridPNMinerPlugin.class);
    	HybridPNMinerSettings settings = getConfiguration();
    	ExtendedHybridPetrinet pn = CGToHybridPN.fuzzyCGToFuzzyPN(cg, settings);
        pn = addMarkings(pluginContext, pn);
    	HybridPetriNetPortObject pnPO = new HybridPetriNetPortObject(pn);
    	logger.info("End: Hybrid Petri Net miner");
    	return pnPO;
    }

    private ExtendedHybridPetrinet addMarkings(PluginContext context, ExtendedHybridPetrinet pn) {
    	Place startPlace = pn.getPlace("start");
        Marking im = new Marking();
        im.add(startPlace);
        Place endPlace = pn.getPlace("end");
        Marking fm = new Marking();
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
		HybridPNMinerSettings settings = new HybridPNMinerSettings();
    	// settings.setThresholdEarlyCancelationIterator(m_settings.t_cancel);
		settings.setPlaceEvalThreshold(m_settings.t_fitness);
		try {
			settings.setFitnessType(getFitnessType());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


    protected PortObjectSpec[] configureOutSpec(CausalGraphPortObjectSpec logSpec) {

        return new PortObjectSpec[]{new HybridPetriNetPortObjectSpec()};
    }

    public PortObject[] getInternalPortObjects() {
		return new PortObject[] {cgPO};
	}

	public void setInternalPortObjects(PortObject[] portObjects) {
		cgPO = (CausalGraphPortObject) portObjects[0];
	}

//	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
//		t_fitness.saveSettingsTo(settings);
//    	t_cancel.saveSettingsTo(settings); 
//    	type_fitness.saveSettingsTo(settings); 
//	}
//
//
//	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
//		t_fitness.loadSettingsFrom(settings);
//		t_cancel.loadSettingsFrom(settings); 
//		type_fitness.loadSettingsFrom(settings); 
//	}
     
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
	
}

