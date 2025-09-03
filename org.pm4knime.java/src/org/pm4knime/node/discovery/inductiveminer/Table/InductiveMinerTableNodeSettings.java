package org.pm4knime.node.discovery.inductiveminer.Table;

import java.util.Arrays;
import java.util.List;

import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.pm4knime.node.discovery.alpha.table.AlphaMinerTableNodeSettings.IsMaxOne;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings;



public final class InductiveMinerTableNodeSettings extends DefaultTableMinerSettings {
	
	public static interface InductiveMinerDialogLayout extends DialogLayout{
	    
		
	    @Section(title = "Inductive Miner Settings")
        @After(MainDropdownSection.class)
        interface Settings {
	    	
        }   

      
	}
	 
	public static final List<String> variantList = Arrays.asList("Inductive Miner - Base", 
			"Inductive Miner - Infrequent", 
			"Inductive Miner - Incompleteness",
			"Inductive Miner - Life cycle" 
	);
	
	
	public static class InductiveMinerChoicesProvider implements StringChoicesProvider {
	    @Override
	    public List<String> choices(final NodeParametersInput context) {
	        return variantList;
	    }
	}
	
	
	@Widget(title = "Inductive Miner Variant", description = "The variant of the Inductive Miner to be used.")
	@Layout(InductiveMinerDialogLayout.Settings.class)
	@ChoicesProvider(value = InductiveMinerChoicesProvider.class)
	String m_variant = variantList.get(1);
	
	
	@Widget(title = "Noise Threshold for Least Frequency", description = "Threshold for filtering out noise. Accepted values: between 0.0 and 1.0.")
	@Layout(InductiveMinerDialogLayout.Settings.class)
	@NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOne.class)
	double m_noise = 0.2;


}
