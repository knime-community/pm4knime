//package org.pm4knime.node.io.bpmn.reader;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.EnumSet;
//import org.knime.core.node.ExecutionContext;
//import org.knime.core.node.InvalidSettingsException;
//import org.knime.core.node.NodeSettingsRO;
//import org.knime.core.node.NodeSettingsWO;
//import org.knime.core.node.context.ports.PortsConfiguration;
//import org.knime.core.node.port.PortObject;
//import org.knime.core.node.port.PortObjectHolder;
//import org.knime.core.node.port.PortObjectSpec;
//import org.knime.core.node.port.PortType;
//import org.knime.core.node.web.ValidationError;
//import org.knime.filehandling.core.connections.FSCategory;
//import org.knime.filehandling.core.connections.FSFiles;
//import org.knime.filehandling.core.connections.FSPath;
//import org.knime.filehandling.core.defaultnodesettings.EnumConfig;
//import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.ReadPathAccessor;
//import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
//import org.knime.filehandling.core.defaultnodesettings.filtermode.SettingsModelFilterMode.FilterMode;
//import org.knime.filehandling.core.defaultnodesettings.status.NodeModelStatusConsumer;
//import org.knime.filehandling.core.defaultnodesettings.status.StatusMessage.MessageType;
//import org.knime.js.core.node.AbstractSVGWizardNodeModel;
//import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
//import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
//import org.pm4knime.portobject.BpmnPortObject;
//import org.pm4knime.portobject.BpmnPortObjectSpec;
//import org.pm4knime.portobject.HybridPetriNetPortObject;
//import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;
//import org.pm4knime.util.defaultnode.ReaderNodeSettings;
//import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
//
//
//public class BpmnReaderNodeModel
//    extends AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> 
//    implements PortObjectHolder {
//    
//	public static final String[] defaultTypes = new String[] {".bpmn"};
//    private static final String SOURCE_FILE = "sourcefile";
//    private static final EnumConfig<FilterMode> mode = EnumConfig.create(FilterMode.FILE);
//	private static final EnumSet<FSCategory> DEFAULT_FS = //
//			EnumSet.of(FSCategory.LOCAL, FSCategory.MOUNTPOINT, FSCategory.RELATIVE, FSCategory.CUSTOM_URL, FSCategory.HUB_SPACE);
//	private final SettingsModelReaderFileChooser m_sourceModel;
//	private final NodeModelStatusConsumer m_statusConsumer = new NodeModelStatusConsumer(
//			EnumSet.of(MessageType.ERROR, MessageType.WARNING));
//	
//	BpmnPortObjectSpec port_obj_spec = new BpmnPortObjectSpec();
//
//	protected BpmnPortObject port_obj;
//	
//    public BpmnReaderNodeModel(PortsConfiguration portsConfiguration) {
//    
//        // TODO as one of those tests
//        super(null, new PortType[] {BpmnPortObject.TYPE}, "BPMN JS View");
//        m_sourceModel = createSourceModel(portsConfiguration);
//    }
//    
//	static final SettingsModelReaderFileChooser createSourceModel(final PortsConfiguration portsConfig) {
//		return new SettingsModelReaderFileChooser(SOURCE_FILE, portsConfig, BpmnReaderNodeFactory.CONNECTION_INPUT_PORT_GRP_NAME, mode, DEFAULT_FS,  defaultTypes);
//	}
//
//    @Override
//    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
//        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
//        return new PortObject[]{port_obj};
//    }
//	
//	@Override
//	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
//		
//		exec.checkCanceled();
//        try {
//			final ReadPathAccessor readAccessor = m_sourceModel.createReadPathAccessor();
//			final FSPath inputPath = readAccessor.getFSPaths(m_statusConsumer).get(0);
//			InputStream inputStream = FSFiles.newInputStream(inputPath);			
//			
//			BPMNDiagram bpmn = BpmnPortObject.importBPMNDiagram(inputStream);
//			
//			exec.checkCanceled();
//			port_obj = new BpmnPortObject(bpmn);
//
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InvalidSettingsException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//        
//        exec.checkCanceled();
//		
//		JSGraphVizViewRepresentation representation = getViewRepresentation();
//
//		representation.setJSONString(port_obj.getJSON());
//
//	}
//
//
//    @Override
//    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
//            throws InvalidSettingsException {
//        
//    	return new PortObjectSpec[]{port_obj_spec};
//    }
//
//   
//    @Override
//    protected void saveSettingsTo(final NodeSettingsWO settings) {
//
//    	m_sourceModel.saveSettingsTo(settings);
//
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
//            throws InvalidSettingsException {
//    	m_sourceModel.loadSettingsFrom(settings);
//      
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    protected void validateSettings(final NodeSettingsRO settings)
//            throws InvalidSettingsException {
//    	m_sourceModel.validateSettings(settings);
//    }
//    
//    @Override
//	protected void performReset() {
//	}
//
//	@Override
//	protected void useCurrentValueAsDefault() {
//	}
//
//	
//	@Override
//    protected boolean generateImage() {
//        return false;
//    }
//	
//	
//	@Override
//	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
//		return new JSGraphVizViewRepresentation();
//	}
//
//	@Override
//	public JSGraphVizViewValue createEmptyViewValue() {
//		return new JSGraphVizViewValue();
//	}
//	
//	@Override
//	public boolean isHideInWizard() {
//		return false;
//	}
//
//	@Override
//	public void setHideInWizard(boolean hide) {
//	}
//
//	@Override
//	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
//		return null;
//	}
//
//	@Override
//	public void saveCurrentValue(NodeSettingsWO content) {
//	}
//	
//	
//	@Override
//	public String getJavascriptObjectID() {
//		return "org.pm4knime.node.visualizations.jsgraphviz.component";
//	}
//
//
//	@Override
//	public PortObject[] getInternalPortObjects() {
//		// TODO Auto-generated method stub
//		return new PortObject[] {};
//	}
//
//
//	@Override
//	public void setInternalPortObjects(PortObject[] portObjects) {
//		
//	}
//    
//
//}
//
