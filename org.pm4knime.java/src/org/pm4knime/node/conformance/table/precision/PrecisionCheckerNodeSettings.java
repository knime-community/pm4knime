package org.pm4knime.node.conformance.table.precision;

import java.util.Arrays;
import java.util.List;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.processmining.plugins.multietc.sett.MultiETCSettings;


public final class PrecisionCheckerNodeSettings implements NodeParameters {
	
	public static interface PrecisionDialogLayout {
	    
		
	    @Section(title = "OrderedPresentation")
        interface OrderedPresentation {
	    	
        }   
	    
	    @Section(title = "Algorithm")
        @After(OrderedPresentation.class)
        interface Algorithm {
        }
      
	 }
	
	final static String ALIGN_1 = "1-Align Precision";
	final static String ALIGN_ALL = "All-Align Precision";
	final static String ALIGN_REPRE = "Representative-Align Precision";
	final static String ETC = "ETC Precision (no invisible/duplicates allowed)";
	
	public static final List<String> algorithmList = Arrays.asList(MultiETCSettings.ALGORITHM.toString() , ALIGN_1,
			ALIGN_ALL, ALIGN_REPRE, ETC);
	 
	public static class AlgorithmChoicesProvider implements StringChoicesProvider {
        @Override
        public List<String> choices(final NodeParametersInput context) {
            return algorithmList;
        }
    }

    
	@Widget(title = "Ordered Representation", description = "The representation of the information in the event log (ordered sequences or multi-sets). This option is enabled by default.")
	@Layout(PrecisionDialogLayout.OrderedPresentation.class)
	boolean m_ignore_ll = true;

    @Widget(title = "Algorithm", description = "The algorithm to calculate the precision. The following options are available:\r\n"
    		+ "			<ul>\r\n"
    		+ "				<li>1-Align Precision.</li>\r\n"
    		+ "				<li>All-Align Precision.</li>\r\n"
    		+ "				<li>Representative-Align Precision.</li>\r\n"
    		+ "				<li>ETC Precision (no invisible/duplicates allowed). </li>\r\n"
    		+ "				</ul>")
    @Layout(PrecisionDialogLayout.Algorithm.class)
    @ChoicesProvider(value = AlgorithmChoicesProvider.class)
    String m_variant = ALIGN_1;

}