package org.pm4knime.node.discovery.ilpminer.Table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.PetriNetPortObject;



public class ILPMinerTableNodeFactory extends WebUINodeFactory<ILPMinerTableNodeModel> implements WizardNodeFactoryExtension<ILPMinerTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	ILPMinerTableNodeModel node;
	
	public static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("ILP Miner")
			.icon("../../category-discovery.png")
			.shortDescription("This node implements the ILP Miner to discover a Petri net from an event table.")
			.fullDescription("This node implements the ILP Miner to discover a Petri net from an event table.\r\n"
					+ PetriNetPortObject.PETRI_NET_TEXT) 
			.modelSettingsClass(ILPMinerTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Petri Net", PetriNetPortObject.TYPE, "a Petri net")//
			.nodeType(NodeType.Learner)
			.build();


	public ILPMinerTableNodeFactory() {
		super(CONFIG);
	}


	protected ILPMinerTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public ILPMinerTableNodeModel createNodeModel() {
		node = new ILPMinerTableNodeModel(ILPMinerTableNodeSettings.class);
		return node;
	}

}

