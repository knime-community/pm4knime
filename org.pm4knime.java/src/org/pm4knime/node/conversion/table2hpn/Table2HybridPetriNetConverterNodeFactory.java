package org.pm4knime.node.conversion.table2hpn;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.HybridPetriNetPortObject;


public class Table2HybridPetriNetConverterNodeFactory extends WebUINodeFactory<Table2HybridPetriNetConverterNodeModel> implements WizardNodeFactoryExtension<Table2HybridPetriNetConverterNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	Table2HybridPetriNetConverterNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Table to Hybrid Petri Net")
			.icon("../category-conversion.png")
			.shortDescription("Convert a KNIME Data Table into a hybrid Petri net")
			.fullDescription("This node converts a KNIME Data Table into a hybrid Petri net. "
					+ HybridPetriNetPortObject.HYBRID_PETRI_NET_TEXT)//
			.modelSettingsClass(EmptyNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Hybrid Petri Net", HybridPetriNetPortObject.TYPE, "a hybrid Petri net")//
			.nodeType(NodeType.Manipulator)
			.build();



	public Table2HybridPetriNetConverterNodeFactory() {
		super(CONFIG);
	}


	protected Table2HybridPetriNetConverterNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public Table2HybridPetriNetConverterNodeModel createNodeModel() {
		node = new Table2HybridPetriNetConverterNodeModel(EmptyNodeSettings.class);
		return node;
	}
}