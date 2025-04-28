package org.pm4knime.node.io.bpmn.reader;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;

@SuppressWarnings("restriction")
public class BpmnReaderNodeFactory extends WebUINodeFactory<BpmnReaderNodeModel> implements
		WizardNodeFactoryExtension<BpmnReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	BpmnReaderNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("BPMN Reader").icon("../../read.png")
			.shortDescription("Import a BPMN model.")
			.fullDescription("<intro>\r\n"
					+ "        Import a Business Process Model and Notation (BPMN) from a BPMN file.\r\n"
					+ "        <br/>\r\n"
					+ "        <br/>\r\n"
					+ "        \r\n"
					+ "    </intro>\r\n"
					+ "    <br/>\r\n"
					+ "    <intro>\r\n"
					+ "        <b>Supported BPMN Elements:</b>\r\n"
					+ "        <br/>\r\n"
					+ "        <ul>\r\n"
					+ "            <li>\r\n"
					+ "                <b>Activities:</b> activities represent the actual work that occurs within a business process. They are depicted as rectangular shapes.\r\n"
					+ "            </li>\r\n"
					+ "            <br/>\r\n"
					+ "            <li>\r\n"
					+ "                <b>Events:</b> they represent something that happens during the course of a business process (while activities represent a work that is performed within the process). Supported Event Types:\r\n"
					+ "                <ul>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>Start Events:</b> depicted as a green circle. \r\n"
					+ "                    </li>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>End Events:</b> depicted as a red circle with a bold border.\r\n"
					+ "                    </li>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>Intermediate Events:</b> depicted as a circle with a double border.\r\n"
					+ "                    </li>\r\n"
					+ "                </ul>\r\n"
					+ "            </li>\r\n"
					+ "            <br/>\r\n"
					+ "            <li>\r\n"
					+ "                <b>Gateways:</b> gateways in BPMN are used to control the flow of execution within a process. They serve various purposes, such as decision-making, splitting the flow into multiple paths, or merging multiple paths into a single path. Gateways are depicted as diamond shapes. Supported Gateway Types:\r\n"
					+ "                <ul>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>EXCLUSIVE:</b> marked with ✕.\r\n"
					+ "                    </li>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>PARALLEL:</b> marked with ✚.\r\n"
					+ "                    </li>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>COMPLEX:</b> marked with ❋.\r\n"
					+ "                    </li>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>INCLUSIVE:</b> marked with ◯.\r\n"
					+ "                    </li>\r\n"
					+ "                    <li>\r\n"
					+ "                        <b>EVENTBASED:</b> marked with ⌾.\r\n"
					+ "                    </li>\r\n"
					+ "                </ul>\r\n"
					+ "            </li>\r\n"
					+ "            <br/>\r\n"
					+ "            <li>\r\n"
					+ "                <b>Flows:</b> Flows in BPMN depict the sequencing of activities within a process. They define the order in which activities are performed and connections between them.\r\n"
					+ "            </li>\r\n"
					+ "        </ul>\r\n"
					+ "    </intro>")
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