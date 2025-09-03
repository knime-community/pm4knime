package org.pm4knime.node.io.processtree.reader;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;


public class ProcessTreeReaderNodeFactory extends WebUINodeFactory<ProcessTreeReaderNodeModel> implements
		WizardNodeFactoryExtension<ProcessTreeReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	ProcessTreeReaderNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("ProcessTree Reader").icon("../../read.png")
			.shortDescription("Import a process tree from a PTML file.")
			.fullDescription("Import a process tree from a PTML file.")
			.modelSettingsClass(ReaderNodeSettings.class)
			.addOutputPort("Process Tree", ProcessTreePortObject.TYPE, "a process tree")
			.nodeType(NodeType.Source).build();

	public ProcessTreeReaderNodeFactory() {
		super(CONFIG);
	}

	protected ProcessTreeReaderNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}

	@Override
	public ProcessTreeReaderNodeModel createNodeModel() {
		node = new ProcessTreeReaderNodeModel(ReaderNodeSettings.class);
		return node;
	}
}