package org.pm4knime.node.discovery.alpha.table;

import java.util.Arrays;
import java.util.List;

import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings;
import org.processmining.alphaminer.parameters.AlphaVersion;



public final class AlphaMinerTableNodeSettings extends DefaultTableMinerSettings {



		 
		public static interface AlphaDialogLayout extends DialogLayout{
		    
			
		    @Section(title = "Alpha Miner Variant")
	        @After(MainDropdownSection.class)
	        interface Variants {
		    	
	        }   
		    
		    @Section(title = "Parameters for AlphaR")
	        @After(Variants.class)
	        interface AlphaRSection {
	        }

	        @Section(title = "Parameters for Alpha+")
	        @After(AlphaRSection.class)
	        interface AlphaPlus {
	        }

	      
		 }
		 
		public static final List<String> variantList = Arrays.asList(AlphaVersion.CLASSIC.toString() , AlphaVersion.PLUS.toString(),
					AlphaVersion.PLUS_PLUS.toString(), AlphaVersion.SHARP.toString(), AlphaVersion.ROBUST.toString());
	
	    
	    public static class AlphaVersionChoicesProvider implements StringChoicesProvider {
	        @Override
	        public List<String> choices(final NodeParametersInput context) {
	            return variantList;
	        }
	    }
	
		
	    @Widget(title = "Alpha Variant", description = "The Alpha miner variant to be applied. Five variants are supported: Alpha, Alpha+, Alpha++, Alpha#, and AlphaR.")
	    @Layout(AlphaDialogLayout.Variants.class)
	    @ChoicesProvider(value = AlphaVersionChoicesProvider.class)
	    String m_variant = variantList.get(0);
	   
		
	    public static final class IsMaxOneHundred extends MaxValidation {

            @Override
            public double getMax() {
                return 100;
            }

        }
	    
	    public static final class IsMaxOne extends MaxValidation {

            @Override
            public double getMax() {
                return 1;
            }

        }
	    
	    @Widget(title = "Noise Threshold for Least Frequency", description = "This parameter sets the least frequency noise threshold for the AlphaR variant.")
	    @Layout(AlphaDialogLayout.AlphaRSection.class)
	    @NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOneHundred.class)
		double m_noiseTLF = 0.0;
		
	    @Widget(title = "Noise Threshold for Most Frequency", description = "This parameter sets the most frequency noise threshold for the AlphaR variant.")
	    @Layout(AlphaDialogLayout.AlphaRSection.class)
	    @NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOneHundred.class)
		double m_noiseTMF = 0.0;
	    
	    @Widget(title = "Casual Threshold", description = "This parameter sets the casual threshold for the AlphaR variant.")
	    @Layout(AlphaDialogLayout.AlphaRSection.class)
	    @NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOneHundred.class)
		double m_casualTH = 0.0;
		 
	    
	    
	    @Widget(title = "Ignore the length of the loops", description = " This parameter sets whether the Alpha+ variant ignores loop length or not.")
	    @Layout(AlphaDialogLayout.AlphaPlus.class)
	    boolean m_ignore_ll = false;
	    

 
	}
