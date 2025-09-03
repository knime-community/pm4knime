package org.pm4knime.node.conversion.table2pn;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;


public class Table2PetriNetConverterNodeFactory extends WebUINodeFactory<Table2PetriNetConverterNodeModel> implements WizardNodeFactoryExtension<Table2PetriNetConverterNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	Table2PetriNetConverterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Table to Petri Net")
			.icon("../category-conversion.png")
			.shortDescription("Convert a KNIME Data Table into a Petri net")
			.fullDescription("This node converts a KNIME Data Table into a Petri net. "
					+ PetriNetPortObject.PETRI_NET_TEXT)//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Petri net", PetriNetPortObject.TYPE, "a Petri net")//
			.nodeType(NodeType.Manipulator)
			.build();



	public Table2PetriNetConverterNodeFactory() {
		super(CONFIG);
	}


	protected Table2PetriNetConverterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public Table2PetriNetConverterNodeModel createNodeModel() {
		node = new Table2PetriNetConverterNodeModel(EmptyNodeSettings.class);
		return node;
	}
}