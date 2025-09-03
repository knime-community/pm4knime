package org.pm4knime.node.io.hybridpetrinet.writer;


import org.knime.core.webui.node.dialog.defaultdialog.internal.file.FileWriterWidget;
import org.knime.node.parameters.Widget;
import org.pm4knime.util.NodeSettingsUtils;
import org.pm4knime.util.defaultnode.WriterNodeSettings;


public final class HybridPetrinetWriterNodeSettings extends WriterNodeSettings {

	@Widget( 
        title = "Output location (path and file name)", 
        description = """
                Specify the full path to where the file shall be written. The location can be either an operating system-dependent path on the local machine or a KNIME URL.
                The folder or workflow group in which the output file shall be written has to exist.
                """ 
    )
	@FileWriterWidget(fileExtension = "pnml")
	String m_outputFile = NodeSettingsUtils.getPathInUserHomeDir("hybrid_petri_net.pnml");

	public String getExtension() {
		// TODO Auto-generated method stub
		return ".pnml";
	}
	
}

