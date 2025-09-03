package org.pm4knime.node.io.bpmn.reader;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;


public class BpmnReaderNodeFactory extends WebUINodeFactory<BpmnReaderNodeModel> implements
		WizardNodeFactoryExtension<BpmnReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	BpmnReaderNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("BPMN Reader").icon("../../read.png")
			.shortDescription("Import a BPMN model.")
			.fullDescription("This node imports a BPMN model from a BPMN file. BPMN, or Business Process Model and Notation, encompasses several key elements that collectively define and illustrate a business process.")
			.modelSettingsClass(ReaderNodeSettings.class)
			.addOutputPort("BPMN", BpmnPortObject.TYPE, "a BPMN model")
			.nodeType(NodeType.Source).build();

	public BpmnReaderNodeFactory() {
		super(CONFIG);
	}

	protected BpmnReaderNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}

	@Override
	public BpmnReaderNodeModel createNodeModel() {
		node = new BpmnReaderNodeModel(ReaderNodeSettings.class);
		return node;
	}
}