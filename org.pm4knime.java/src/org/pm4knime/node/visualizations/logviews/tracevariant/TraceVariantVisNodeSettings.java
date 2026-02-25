package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.StringCellColumnsProvider;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.TimeColumnsProvider;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputColumnHandlingMode;

public final class TraceVariantVisNodeSettings implements NodeParameters {

	public interface DialogLayout {

		@Section(title = "Event Log Classifiers")
		interface MainDropdownSection {
		}

		@Section(title = "Variant ID")
		interface VariantIdSection {
		}
	}

	@Widget(title = "Case ID", description = "The column that contains the case/trace identifiers.")
	@Layout(DialogLayout.MainDropdownSection.class)
	@ChoicesProvider(value = StringCellColumnsProvider.class)
	public String t_classifier;

	@Widget(title = "Activity", description = "The column that contains the activity/event identifiers.")
	@Layout(DialogLayout.MainDropdownSection.class)
	@ChoicesProvider(value = StringCellColumnsProvider.class)
	public String e_classifier;

	@Widget(title = "Timestamp", description = "The column that contains the timestamps.")
	@Layout(DialogLayout.MainDropdownSection.class)
	@ChoicesProvider(value = TimeColumnsProvider.class)
	public String time_classifier;

	@Widget(title = "Variant ID", description = "The name of the column that will be appended to the event log containing the variant ID for each trace.")
	@Layout(DialogLayout.VariantIdSection.class)
	public String variantIdColumnName = "Variant ID";

	@Widget(title = "If column already exists", description = "Defines the behaviour if a column with the specified name already exists in the input table. "
			+ "Select 'Fail' to stop execution, or 'Overwrite' to replace the existing column.")
	@ValueSwitchWidget
	@Layout(DialogLayout.VariantIdSection.class)
	public ExistingOutputColumnHandlingMode existingVariantIdColumnMode = ExistingOutputColumnHandlingMode.FAIL;
}