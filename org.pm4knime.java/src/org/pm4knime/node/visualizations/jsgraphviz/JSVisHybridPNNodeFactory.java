package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;


public class JSVisHybridPNNodeFactory extends WebUINodeFactory<JSGraphVizAbstractModel> implements WizardNodeFactoryExtension<JSGraphVizAbstractModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	
	JSGraphVizAbstractModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Hybrid Petri Net To Image")
			.icon("./process.png")
			.shortDescription("JavaScript Visualizer for hybrid Petri nets.")
			.fullDescription("This node implements a JavaScript visualization of hybrid Petri nets. By default, informal edges are colored as follows: blue arcs for certain edges, red arcs for uncertain edges, and yellow arcs for long-term dependency edges.")//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Hybrid Petri Net", HybridPetriNetPortObject.TYPE ,"a hybrid Petri net")//
			.addOutputPort("Image", ImagePortObject.TYPE, "an SVG image")//
			.nodeType(NodeType.Visualizer)
			.build();

	public JSVisHybridPNNodeFactory() {
		super(CONFIG);
	}


	protected JSVisHybridPNNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public JSGraphVizAbstractModel createNodeModel() {
		PortType[] IN_TYPES = {HybridPetriNetPortObject.TYPE};
		node = new JSGraphVizAbstractModel(IN_TYPES, "Hybrid Petri Net JS View", EmptyNodeSettings.class);
		return node;
	}
}