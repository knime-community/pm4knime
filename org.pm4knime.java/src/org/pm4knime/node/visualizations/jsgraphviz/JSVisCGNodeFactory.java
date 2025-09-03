package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;


public class JSVisCGNodeFactory extends WebUINodeFactory<JSGraphVizAbstractModel> implements WizardNodeFactoryExtension<JSGraphVizAbstractModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	
	JSGraphVizAbstractModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Causal Graph To Image")
			.icon("./dfg.png")
			.shortDescription("JavaScript Visualizer for Causal Graphs.")
			.fullDescription("This node implements a JavaScript visualization of causal graphs. A causal graph consists of nodes representing activities and two types of directed edges connecting nodes. \r\n"
					+ "			Certain edges (blue by default) represent strong causal dependencies and uncertain edges (red by default) represent weak dependencies. \r\n"
					+ "			A third type of edges is used to represent long-term dependencies (yellow by default).")//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Causal Graph", CausalGraphPortObject.TYPE ,"a causal graph")//
			.addOutputPort("Image", ImagePortObject.TYPE, "an SVG image")//
			.nodeType(NodeType.Visualizer)
			.build();

	public JSVisCGNodeFactory() {
		super(CONFIG);
	}


	protected JSVisCGNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public JSGraphVizAbstractModel createNodeModel() {
		PortType[] IN_TYPES = {CausalGraphPortObject.TYPE};
		node = new JSGraphVizAbstractModel(IN_TYPES, "Causal Graph JS View", EmptyNodeSettings.class);
		return node;
	}
}