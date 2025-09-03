package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;


public class JSGraphVizPTNodeFactory extends WebUINodeFactory<JSGraphVizAbstractModel> implements WizardNodeFactoryExtension<JSGraphVizAbstractModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	
	JSGraphVizAbstractModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Process Tree To Image")
			.icon("./tree.png")
			.shortDescription("JavaScript Visualizer for Process Trees")
			.fullDescription("This node implements a JavaScript visualization of Process Trees.\r\n"
					+ "    <br />\r\n"
					+ "    A process tree is a block-structured process model where the (inner) nodes are operators (sequence, choice, parallel, and loop) and the leaves are activities. \r\n"
					+ "    <br /> \r\n"
					+ "    The \"seq\" operator executes its children from right to left.\r\n"
					+ "    <br />\r\n"
					+ "    The \"xor\" operator executes one of its children.\r\n"
					+ "    <br />\r\n"
					+ "    The \"and\" operator executes the children in parallel.\r\n"
					+ "    <br />\r\n"
					+ "    The \"xor loop\" operator models a do-redo loop. The first child is used as the do part, while an exclusive choice between the other children is used as the redo part.")//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Process Tree", ProcessTreePortObject.TYPE ,"a process tree")//
			.addOutputPort("Image", ImagePortObject.TYPE, "an SVG image")//
			.nodeType(NodeType.Visualizer)
			.build();

	public JSGraphVizPTNodeFactory() {
		super(CONFIG);
	}


	protected JSGraphVizPTNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public JSGraphVizAbstractModel createNodeModel() {
		PortType[] IN_TYPES = {ProcessTreePortObject.TYPE};
		node = new JSGraphVizAbstractModel(IN_TYPES, "Process Tree JS View", EmptyNodeSettings.class);
		return node;
	}
}