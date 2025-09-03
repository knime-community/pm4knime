package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.pm4knime.node.discovery.alpha.table.AlphaMinerTableNodeSettings.IsMaxOne;



public final class InductiveMinerDFGTableNodeSettings implements NodeParameters {
	
	 @Widget(title = "Noise Threshold", description = "Threshold for filtering out noise.")
	 @NumberInputWidget(minValidation=IsNonNegativeValidation.class, maxValidation=IsMaxOne.class)
	 double m_noiseThreshold = 0.8;	 
}

