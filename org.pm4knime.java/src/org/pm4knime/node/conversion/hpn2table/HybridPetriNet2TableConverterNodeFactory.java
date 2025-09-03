package org.pm4knime.node.conversion.hpn2table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.HybridPetriNetPortObject;



public class HybridPetriNet2TableConverterNodeFactory extends WebUINodeFactory<HybridPetriNet2TableConverterNodeModel> {

	HybridPetriNet2TableConverterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Hybrid Petri Net to Table")
			.icon("../category-conversion.png")
			.shortDescription("Convert a hybrid Petri net into a KNIME Data Table")
			.fullDescription("This node converts a hybrid Petri net into a KNIME data table.")//
			.modelSettingsClass(HybridPetriNet2TableConverterNodeSettings.class)//
			.addInputPort("Hybrid Petri net", HybridPetriNetPortObject.TYPE, "a hybrid Petri net")//
			.addOutputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.nodeType(NodeType.Manipulator)
			.build();

	public HybridPetriNet2TableConverterNodeFactory() {
		super(CONFIG);
	}


	protected HybridPetriNet2TableConverterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public HybridPetriNet2TableConverterNodeModel createNodeModel() {
		node = new HybridPetriNet2TableConverterNodeModel(HybridPetriNet2TableConverterNodeSettings.class);
		return node;
	}
}