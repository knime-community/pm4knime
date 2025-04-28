//package org.pm4knime.node.io.bpmn.reader;
//
//import org.knime.core.node.context.ports.PortsConfiguration;
//import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
//import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;
//import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.DialogComponentReaderFileChooser;
//import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
//
//
//
//public class BpmnReaderNodeDialog extends DefaultNodeSettingsPane {
//
//	// Constructor for BpmnReaderNodeDialog
//	protected BpmnReaderNodeDialog(PortsConfiguration portsConfiguration) {
//	    
//		// Create a SettingsModelReaderFileChooser for the source model
//		SettingsModelReaderFileChooser m_source = BpmnReaderNodeModel.createSourceModel(portsConfiguration);
//		this.setHorizontalPlacement(true);
//
//		// Create a DialogComponentReaderFileChooser with the source model
//        // and a description for the component
//		DialogComponentReaderFileChooser fileCompAdvanced = new DialogComponentReaderFileChooser(m_source,
//				"copy-source",
//				createFlowVariableModel(m_source.getKeysForFSLocation(), FSLocationVariableType.INSTANCE));
//		
//		// Add the file chooser component to the dialog
//		addDialogComponent(fileCompAdvanced);
//
//
//	}
//}