package org.pm4knime.node.conversion.pt2pn;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;


public class PT2PNConverterNodeFactory extends WebUINodeFactory<PT2PNConverterNodeModel> implements
		WizardNodeFactoryExtension<PT2PNConverterNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	PT2PNConverterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Process Tree to Petri Net").icon("../category-conversion.png")
			.shortDescription("Convert a process tree into a Petri net.")
			.fullDescription("This node converts a process tree into a Petri net. \r\n"
					+ "        No configuration is needed to complete the conversion. \r\n" + "        <br />\r\n"
					+ "    A process tree is a block-structured process model where the (inner) nodes are operators (sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
					+ "    <br /> \r\n"
					+ PetriNetPortObject.PETRI_NET_TEXT)//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Process Tree", ProcessTreePortObject.TYPE, "a process tree")//
			.addOutputPort("Petri net", PetriNetPortObject.TYPE, "a Petri net")//
			.nodeType(NodeType.Manipulator).build();

	public PT2PNConverterNodeFactory() {
		super(CONFIG);
	}

	protected PT2PNConverterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}

	@Override
	public PT2PNConverterNodeModel createNodeModel() {
		node = new PT2PNConverterNodeModel(EmptyNodeSettings.class);
		return node;
	}
}