package org.pm4knime.node.discovery.alpha.table;



import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.PetriNetPortObject;



public class AlphaMinerTableNodeFactory extends WebUINodeFactory<AlphaMinerTableNodeModel> implements WizardNodeFactoryExtension<AlphaMinerTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	AlphaMinerTableNodeModel node;

	public static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Alpha Miner")
			.icon("../../category-discovery.png")
			.shortDescription("This node implements the Alpha Miner to discover a Petri net from an event table.")
			.fullDescription(" This node implements the Alpha Miner to discover a Petri net from an event table. "
					+ PetriNetPortObject.PETRI_NET_TEXT)//
			.modelSettingsClass(AlphaMinerTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Petri Net", PetriNetPortObject.TYPE, "a Petri net")//
			.nodeType(NodeType.Learner)
			.build();


	public AlphaMinerTableNodeFactory() {
		super(CONFIG);
	}


	protected AlphaMinerTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public AlphaMinerTableNodeModel createNodeModel() {
		node = new AlphaMinerTableNodeModel(AlphaMinerTableNodeSettings.class);
		return node;
	}

}
