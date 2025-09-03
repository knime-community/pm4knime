package org.pm4knime.util.defaultnode;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.Widget;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.StringCellColumnsProvider;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.TimeColumnsProvider;



public class DefaultTableNodeSettings implements NodeParameters {

	
	public interface DialogLayoutWithTime {
		 
        @Section(title = "Event Log Classifiers")
        interface EventLogClassifiers {
        }
      }

	
	@Layout(DialogLayoutWithTime.EventLogClassifiers.class)
	@Widget(title = "Case ID", description = "The column that contains the case/trace identifiers.")
    @ChoicesProvider(value = StringCellColumnsProvider.class)
	public String t_classifier;
	
	@Layout(DialogLayoutWithTime.EventLogClassifiers.class)
    @Widget(title = "Activity", description = "The column that contains the activity/event identifiers.")
	@ChoicesProvider(value = StringCellColumnsProvider.class)
	public String e_classifier;
	
	@Layout(DialogLayoutWithTime.EventLogClassifiers.class)
	@Widget(title = "Timestamp", description = "The column that contains the timestamps.")
	@ChoicesProvider(value = TimeColumnsProvider.class)
	public String time_classifier;

	
}
