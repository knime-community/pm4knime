package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;


public class JSGraphVizDFGNodeFactory extends WebUINodeFactory<JSGraphVizAbstractModel> implements WizardNodeFactoryExtension<JSGraphVizAbstractModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	
	JSGraphVizAbstractModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("DFG To Image")
			.icon("./dfg.png")
			.shortDescription("JavaScript Visualizer for Directly-Follows Graphs")
			.fullDescription("This node implements a JavaScript visualization of directly follows graphs (DFGs). \r\n"
					+ "		    <br/>\r\n"
					+ "		    The green nodes are the start activities and the red activities are the end activity. \r\n"
					+ "		    The edges are annotated by the absolute frequencies of the directly follows relations between the activities.")//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Directly-Follows Graph", DfgMsdPortObject.TYPE ,"a directly follows graph")//
			.addOutputPort("Image", ImagePortObject.TYPE, "an SVG image")//
			.nodeType(NodeType.Visualizer)
			.build();

	public JSGraphVizDFGNodeFactory() {
		super(CONFIG);
	}


	protected JSGraphVizDFGNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public JSGraphVizAbstractModel createNodeModel() {
		PortType[] IN_TYPES = {DfgMsdPortObject.TYPE};
		node = new JSGraphVizAbstractModel(IN_TYPES, "DFG JS View", EmptyNodeSettings.class);
		return node;
	}
}