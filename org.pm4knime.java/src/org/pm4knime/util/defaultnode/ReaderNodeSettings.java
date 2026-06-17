package org.pm4knime.util.defaultnode;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.widget.file.FileSelection;


public class ReaderNodeSettings implements NodeParameters {
	
    @Widget(title = "File Location", description = "Path to the file to read.")
    public FileSelection m_file = new FileSelection();

}
