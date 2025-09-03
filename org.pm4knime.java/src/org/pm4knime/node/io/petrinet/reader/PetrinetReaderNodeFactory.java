package org.pm4knime.node.io.petrinet.reader;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;


public class PetrinetReaderNodeFactory extends WebUINodeFactory<PetrinetReaderNodeModel> implements
		WizardNodeFactoryExtension<PetrinetReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	PetrinetReaderNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Petri Net Reader").icon("../../read.png")
			.shortDescription("Import a Petri net from a PNML file.")
			.fullDescription("This node imports a Petri net from a PNML file. A Petri net is a directed bipartite graph used to model processes. It consists of places, transitions, and directed arcs connecting them. A place is enabled if it it contains at least one token. A transition can only fire if all incoming places are enabled. After firing a transition, a token is consumed from all of its incoming places, and a token is produced in all of its outgoing places. The initial marking indicates the initial state of the Petri net. Places that belong to the initial marking are marked by green tokens inside them. The final marking denotes the final state of the Petri net. Places within the final marking are highlighted with a heavier border.")
			.modelSettingsClass(ReaderNodeSettings.class)
			.addOutputPort("Petri net", PetriNetPortObject.TYPE, "a Petri net")
			.nodeType(NodeType.Source).build();

	public PetrinetReaderNodeFactory() {
		super(CONFIG);
	}

	protected PetrinetReaderNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}

	@Override
	public PetrinetReaderNodeModel createNodeModel() {
		node = new PetrinetReaderNodeModel(ReaderNodeSettings.class);
		return node;
	}
}