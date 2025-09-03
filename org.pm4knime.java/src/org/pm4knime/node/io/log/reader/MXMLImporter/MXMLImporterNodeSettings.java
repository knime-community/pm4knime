package org.pm4knime.node.io.log.reader.MXMLImporter;

import org.knime.core.webui.node.dialog.defaultdialog.internal.file.FileSelection;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;


@SuppressWarnings("restriction")
public class MXMLImporterNodeSettings implements NodeParameters {

	@Widget(title = "File Location", description = "Path to the file to read.")
	public FileSelection m_file = new FileSelection();

}
