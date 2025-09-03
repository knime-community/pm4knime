package org.pm4knime.node.conversion.pn2table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.node.parameters.NodeParameters;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.PetriNetPortObject;



public class PetriNet2TableConverterNodeFactory extends WebUINodeFactory<PetriNet2TableConverterNodeModel> {

	PetriNet2TableConverterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Petri Net to Table")
			.icon("../category-conversion.png")
			.shortDescription("Convert a Petri net into a KNIME Data Table")
			.fullDescription("This node converts a Petri net into a KNIME data table.")//
			.modelSettingsClass(PetriNet2TableConverterNodeSettings.class)//
			.addInputPort("Petri net", PetriNetPortObject.TYPE, "a Petri net")//
			.addOutputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.nodeType(NodeType.Manipulator)
			.build();

	public PetriNet2TableConverterNodeFactory() {
		super(CONFIG);
	}


	protected PetriNet2TableConverterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public PetriNet2TableConverterNodeModel createNodeModel() {
		node = new PetriNet2TableConverterNodeModel(PetriNet2TableConverterNodeSettings.class);
		return node;
	}
}