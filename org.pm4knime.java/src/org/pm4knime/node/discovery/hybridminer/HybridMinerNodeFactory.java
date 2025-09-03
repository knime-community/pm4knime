package org.pm4knime.node.discovery.hybridminer;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObject;


public final class HybridMinerNodeFactory extends WebUINodeFactory<HybridMinerNodeModel> implements WizardNodeFactoryExtension<HybridMinerNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	HybridMinerNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Hybrid Petri Net Miner")
			.icon("../category-discovery.png")
			.shortDescription("This node implements the second step of the Hybrid Miner to discover a hybrid Petri net from a causal graph.")
			.fullDescription("This node implements the second step of the Hybrid Miner to discover a hybrid Petri net from a causal graph. "
					+ "The hybrid miner converts the edges of the input causal graph into formal places if there is enough evidence in the data justifying adding formal constraints. "
					+ "A hybrid Petri net can also contain arcs directly connecting transitions to indicate informal dependencies between them. "
					+ "We distinguish three types of informal arcs: (1) strong dependencies are represented by blue solid arcs (certain arcs); "
					+ "(2) weak dependencies are represented by red dotted arcs (uncertain arcs); (3) long-term dependencies are represented by orange solid arcs.")
			.modelSettingsClass(HybridMinerNodeSettings.class)//
			.addInputPort("Causal Graph", CausalGraphPortObject.TYPE ,"a causal graph")//
			.addOutputPort("Hybrid Petri Net", HybridPetriNetPortObject.TYPE, "a hybrid Petri net")//
			.nodeType(NodeType.Learner)
			.build();


	public HybridMinerNodeFactory() {
		super(CONFIG);
	}


	protected HybridMinerNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public HybridMinerNodeModel createNodeModel() {
		node = new HybridMinerNodeModel(HybridMinerNodeSettings.class);
		return node;
	}

}

