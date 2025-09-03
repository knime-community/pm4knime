package org.pm4knime.node.conformance.table.performance;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.Widget;


public final class PerformanceCheckerNodeSettings implements NodeParameters {
	
	public static interface PerformanceDialogLayout {
	    
        interface SynMoves {
	    	
        }   
	    
	    @After(SynMoves.class)
        interface UnreliableReplay {
        }
      
	 }

    
	@Widget(title = "Consider performance in syn moves", description = "Choose whether to consider syn moves. This option is enabled by default.")
	@Layout(PerformanceDialogLayout.SynMoves.class)
	boolean m_withSynMoveComp = true;
	
	@Widget(title = "Include unreliable replay results", description = "Choose whether to include unreliable replay results. This option is enabled by default.")
	@Layout(PerformanceDialogLayout.UnreliableReplay.class)
	boolean m_withUnreliableResultComp = true;

}    