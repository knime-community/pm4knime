package org.pm4knime.node.io.petrinet.writer;


import org.knime.node.parameters.Widget;
import org.knime.node.parameters.widget.file.FileSelection;
import org.knime.node.parameters.widget.file.FileWriterWidget;
import org.pm4knime.util.defaultnode.WriterNodeSettings;


public final class PetrinetWriterNodeSettings extends WriterNodeSettings {

	@Widget( 
        title = "Output location (path and file name)", 
        description = """
                Specify the full path to where the file shall be written. The location can be either an operating system-dependent path on the local machine or a KNIME URL.
                The folder or workflow group in which the output file shall be written has to exist.
                """ 
    )
	
	@FileWriterWidget(fileExtension = "pnml")
	public FileSelection m_outputFile = new FileSelection();
	
	public String getExtension() {
		// TODO Auto-generated method stub
		return ".pnml";
	}
	
}

