package org.pm4knime.node.logmanipulation.merge.table;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;



public final class MergeTableNodeFactory extends WebUINodeFactory<MergeTableNodeModel> {

	MergeTableNodeModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Merge Event Tables")
			.icon("../../category-manipulation.png")
			.shortDescription("This node merges two event tables.")
			.fullDescription("This node merges two event tables.")
			.modelSettingsClass(MergeTableNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"The first event table to be merged (used as the base event table).")//
			.addInputPort("Table", BufferedDataTable.TYPE ,"The second event table to be merged.")//
			.addOutputPort("Table", BufferedDataTable.TYPE, "The merged event table.")//
			.nodeType(NodeType.Manipulator)
			.build();


	public MergeTableNodeFactory() {
		super(CONFIG);
	}


	protected MergeTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}

	@Override
	public MergeTableNodeModel createNodeModel() {
		node = new MergeTableNodeModel();
		return node;
	}

}

