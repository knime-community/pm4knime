package org.pm4knime.node.conformance.replayer.table.helper;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.RepResultPortObjectTable;



public class PNReplayerTableNodeFactory extends WebUINodeFactory<DefaultPNReplayerTableModel> {

	DefaultPNReplayerTableModel node;

	private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Alignment-Based Replayer")
			.icon("../../../category-conformance.png")
			.shortDescription("This node implements the alignment-based replayer for constructing the optimal alignments by replaying an event log on a Petri net.")
			.fullDescription("This node implements the alignment-based replayer, which accepts an event log and a Petri net as input and outputs the optimal alignments of the traces after replaying the event log on the Petri net.	\r\n"
					+ "		Alignment-based replay is the state-of-the-art technique in conformance checking.\r\n"
					+ "		Alignments provide a robust and detailed view on the deviations between the log and the model. \r\n"
					+ "		\r\n"
					+ "		Three types of moves are considered while constructing the alignments:\r\n"
					+ "		<ul>\r\n"
					+ "		<li>Sync Move: <br/>\r\n"
					+ "		The classification of the current event corresponds to the firing transitions in Petri net.\r\n"
					+ "		In this case, during replaying, both the trace and the process model move to next comparison.\r\n"
					+ "		</li>\r\n"
					+ "  \r\n"
					+ "				<li>Log Move:<br/>\r\n"
					+ "				The classification of the current event doesn't have any\r\n"
					+ "					corresponding firing transitions in Petri net.\r\n"
					+ "					During replaying, the trace moves forward but the state of process model doesn't change.\r\n"
					+ "				</li>\r\n"
					+ "\r\n"
					+ "				<li>Model Move:<br/>\r\n"
					+ "				The firing transitions in Petri net have no corresponding event in the trace.\r\n"
					+ "					During replaying, the model moves forwards but the state of the event log doesn't change.\r\n"
					+ "				</li>\r\n"
					+ "			</ul>\r\n"
					+ "		<br />\r\n"
					+ "		For more information:\r\n"
					+ "			<a href=\"https://ieeexplore.ieee.org/abstract/document/6037560\">link</a>	\r\n"
					+ "		")
			.modelSettingsClass(PNReplayerTableNodeSettings.class)//
			.addInputPort("Event Log", BufferedDataTable.TYPE ,"an event log")//
			.addInputPort("Petri Net", PetriNetPortObject.TYPE ,"a Petri net")//
			.addOutputPort("Replay Result", RepResultPortObjectTable.TYPE, "replay result")//
			.nodeType(NodeType.Other)
			.build();


	public PNReplayerTableNodeFactory() {
		super(CONFIG);
	}


	protected PNReplayerTableNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public DefaultPNReplayerTableModel createNodeModel() {
		node = new DefaultPNReplayerTableModel(PNReplayerTableNodeSettings.class);
		return node;
	}

}

