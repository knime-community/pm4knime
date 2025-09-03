package org.pm4knime.node.discovery.defaultminer;

import java.util.Arrays;
import java.util.Collection;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeValue;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.choices.util.FilteredInputTableColumnsProvider;
import org.knime.node.parameters.Widget;

 
public class DefaultTableMinerSettings implements NodeParameters {	
	
	
	public interface DialogLayout {
		 
        @Section(title = "Event Log Classifiers")
        interface MainDropdownSection {
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
	
	@Layout(DialogLayout.MainDropdownSection.class)
	@Widget(title = "Timestamp", description = "The column that contains the timestamps.")
	@ChoicesProvider(value = TimeColumnsProvider.class)
	public String time_classifier;
		
		
    public static class TimeColumnsProvider extends CompatibleColumnsProvider {

    	private static final Collection<Class<? extends DataValue>> TIME_TYPES = Arrays.asList(
    			ZonedDateTimeValue.class, LocalDateTimeValue.class
		);
    	
    	protected TimeColumnsProvider() {
			super(TIME_TYPES);
		}

    }
    
    public static class StringCellColumnsProvider implements FilteredInputTableColumnsProvider {

        @Override
        public boolean isIncluded(final DataColumnSpec col) {
            final var colType = col.getType();
            final var cellClass = colType.getCellClass();
            return cellClass.isAssignableFrom(StringCell.class);
        }
    }
    

}
