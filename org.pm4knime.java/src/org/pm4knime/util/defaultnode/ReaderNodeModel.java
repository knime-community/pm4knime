package org.pm4knime.util.defaultnode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.connections.FSFileSystem;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.defaultnodesettings.FileSystemHelper;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.node.visualizations.jsgraphviz.util.WebUIJSViewNodeModel;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.XLogPortObject;


public abstract class ReaderNodeModel extends WebUIJSViewNodeModel<ReaderNodeSettings, JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {
	
	
	private ReaderNodeSettings m_settings;
	String[] extensions;
    
	PortObjectSpec m_spec;
	protected AbstractJSONPortObject m_Port;
	protected XLogPortObject m_logPO;
	
    public ReaderNodeModel(Class<ReaderNodeSettings> class1, String[] types, PortObjectSpec portObjectSpec, PortType[] portTypes, String view_name) {
    
    	super(null, portTypes, view_name, class1);
    	m_spec = portObjectSpec; 
    	extensions = types;
    }

	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{m_Port};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		exec.checkCanceled();
        try {       	
        	
        	var fsLocation = m_settings.m_file.getFSLocation();
            FSConnection connection = FileSystemHelper.retrieveFSConnection(Optional.empty(), fsLocation)
                    .orElseThrow(() -> new IOException("File system is not available"));
            FSFileSystem<?> fileSystem = connection.getFileSystem();
			final Path filePath = fileSystem.getPath(fsLocation);
			InputStream inputStream = FSFiles.newInputStream(filePath);
			
			m_Port = write_file_from_stream(inputStream);
			
			JSGraphVizViewRepresentation representation = getViewRepresentation();
			representation.setJSONString(m_Port.getJSON());
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        exec.checkCanceled();
        
     } 
 
	protected abstract AbstractJSONPortObject write_file_from_stream(InputStream inputStream);
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs, final ReaderNodeSettings modelSettings) throws InvalidSettingsException {
		m_settings = modelSettings;
		validate();
		return configureOutSpec();
	}
	
	public void validate() throws InvalidSettingsException {

		if (StringUtils.isEmpty(m_settings.m_file.getFSLocation().getPath())) {
			throw new InvalidSettingsException("Please specify a path to the file to read!");
		}

		if (!StringUtils.endsWith(m_settings.m_file.getFSLocation().getPath(), extensions[0])) {
			throw new InvalidSettingsException("Unsupported file type: Please select a " + extensions[0] + " file");
		}

	}
	
	protected abstract PortObjectSpec[] configureOutSpec();

    
    @Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	
	@Override
    protected boolean generateImage() {
        return false;
    }
	
	
	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
	}
	
	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}
	
	
	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
	}


	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {};
	}


	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		
	}



}

