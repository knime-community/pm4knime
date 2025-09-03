package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.StringCellColumnsProvider;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.DialogLayout;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.TimeColumnsProvider;



public final class TraceVariantVisNodeSettings implements NodeParameters {

	@Widget(title = "Case ID", description = "The column that contains the case/trace identifiers.")
	@Layout(DialogLayout.MainDropdownSection.class)
	@ChoicesProvider(value = StringCellColumnsProvider.class)
	public String t_classifier;
	
	@Widget(title = "Activity", description = "The column that contains the activity/event identifiers.")
	@Layout(DialogLayout.MainDropdownSection.class)
	@ChoicesProvider(value = StringCellColumnsProvider.class)
	public String e_classifier;

	@Layout(DialogLayout.MainDropdownSection.class)
	@Widget(title = "Timestamp", description = "The column that contains the timestamps.")
	@ChoicesProvider(value = TimeColumnsProvider.class)
	String time_classifier;
	
}