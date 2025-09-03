package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation;
import org.pm4knime.util.defaultnode.DefaultTableNodeSettings;



public final class FilterByLengthTableNodeSettings extends DefaultTableNodeSettings {

	 
	public static interface ExtendedDialogLayout extends DialogLayoutWithTime {
	    
		
	    @Section(title = "Filtering Settings")
        @After(EventLogClassifiers.class)
        interface Settings {
	    	
        }   
	     
	 }
	
	
	@Widget(title = "Keep", description = "The filtering strategy. If \"keep\"\" is chosen, the traces that meet the filtering threshold will be kept.\r\n"
			+ "        Otherwise, the traces that meet the filtering threshold will removed.")
	@Layout(ExtendedDialogLayout.Settings.class)
	boolean m_isKeep = true;
	
	
	@Widget(title = "Minimum Trace Length", description = "The minimum length of trace. It is a positive integer. \r\n"
			+ "        When a negative value is given, the default value 1 is used instead. \r\n"
			+ "        When a double value is given, it is casted down.")
	@Layout(ExtendedDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsMinOne.class)
	int m_minLength = 1;
	
	public static final class IsMinOne extends MinValidation {

        @Override
        public double getMin() {
            return 1;
        }

    }
	
	@Widget(title = "Maximum Trace Length", description = "The maximum length of trace. It is a positive integer.\r\n"
			+ "        When a negative value is given, the default value 1 is automatically set. \r\n"
			+ "        When a double value is given, it is casted down.")
	@Layout(ExtendedDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsMinOne.class)
	int m_maxLength = 20;
	

}
