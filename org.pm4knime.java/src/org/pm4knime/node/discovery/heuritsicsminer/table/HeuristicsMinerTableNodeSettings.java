package org.pm4knime.node.discovery.heuritsicsminer.table;

import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.Widget;
import org.pm4knime.node.discovery.alpha.table.AlphaMinerTableNodeSettings.IsMaxOne;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings;



public final class HeuristicsMinerTableNodeSettings extends DefaultTableMinerSettings {

	public static interface HeuristicsMinerDialogLayout extends DialogLayout{
		
	    @Section(title = "Heuristics Miner Settings")
        @After(MainDropdownSection.class)
        interface Settings {
	    	
        }        
	}

	@Widget(title = "All Tasks Connected", description = "If enabled, every task must have at least one input and one output arc (except the initial and the final activity).")
	@Layout(HeuristicsMinerDialogLayout.Settings.class)
	boolean m_allConnected = true;
	
	@Widget(title = "Long Distance Dependency", description = "If enabled, long distance dependencies are shown in the model.")
	@Layout(HeuristicsMinerDialogLayout.Settings.class)
	boolean m_withLT = false;
	
	@Widget(title = "Threshold: Relative-to-Best", description = "The percentage for the admissible distance between directly follows relations for an activity and the activity's best one. \r\n"
			+ "        At 0, only the best directly follows relation will be shown for every activity. \r\n"
			+ "        At 100, all will be shown.")
	@Layout(HeuristicsMinerDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOne.class)
	double m_r2b = 0.05;
	
	@Widget(title = "Threshold: Dependency", description = "A threshold for the strength of the directly follows relation.")
	@Layout(HeuristicsMinerDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOne.class)
	double m_dependency = 0.9;
	
	@Widget(title = "Threshold: Length-One-loops", description = "A threshold for the L1L metric.")
	@Layout(HeuristicsMinerDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOne.class)
	double m_length1Loop = 0.9;
	
	@Widget(title = "Threshold: Length-Two-loops", description = "A threshold for the L1L metric.")
	@Layout(HeuristicsMinerDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOne.class)
	double m_length2Loop = 0.9;
	
	@Widget(title = "Threshold: Long Distance", description = "A threshold for the strength of the eventually follows relation.")
	@Layout(HeuristicsMinerDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOne.class)
	double m_longDistance = 0.9;
	

}

