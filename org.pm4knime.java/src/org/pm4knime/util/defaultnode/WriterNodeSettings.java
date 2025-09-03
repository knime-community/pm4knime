package org.pm4knime.util.defaultnode;


import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputFileHandlingMode;




public class WriterNodeSettings implements NodeParameters {
   
    @Widget( //
        title = "If output file exists", //
        description = "Specify whether the local output file should be overwritten if it exists or whether this node "
            + "should fail. If a KNIME URL is given, no check is done."//
    )
    @ValueSwitchWidget
	public ExistingOutputFileHandlingMode m_existingFileHandlingMode = ExistingOutputFileHandlingMode.FAIL;

}