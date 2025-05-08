package org.pm4knime.portobject;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;


public class BpmnPortObject extends AbstractJSONPortObject {

	
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(BpmnPortObject.class);
	public static final PortType TYPE_OPTIONAL = PortTypeRegistry.getInstance().getPortType(BpmnPortObject.class, true);

	private static final String ZIP_ENTRY_NAME = "BpmnPortObject";

	static String model_xml;
	static boolean enable_auto_layout;
	BpmnPortObjectSpec m_spec;

	public BpmnPortObject() {
	}

	public BpmnPortObject(String bpmn) {
		model_xml = bpmn;
		enable_auto_layout = true;
	}
	
	
	public String getBPMN() {
		return model_xml;
	}

	
	public void setBPMN(String model) {
		model_xml = model;

	}
	
	public void disable_auto_layout() {
		enable_auto_layout = false;
	}

	@Override
	public String getSummary() {
		return model_xml;
	}

	
	public boolean equals(Object o) {
		return model_xml.equals(o);
	}

	@Override
	public BpmnPortObjectSpec getSpec() {
		if (m_spec != null)
			return m_spec;
		return new BpmnPortObjectSpec();
	}

	public void setSpec(PortObjectSpec spec) {
		m_spec = (BpmnPortObjectSpec) spec;
	}

	@Override
	public JComponent[] getViews() {

		return new JComponent[] {};
	}


	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		
		byte[] xmlBytes = model_xml.getBytes(StandardCharsets.UTF_8);

	    // 2) start the ZIP entry
	    ZipEntry entry = new ZipEntry(ZIP_ENTRY_NAME);
	    // (optional) help ZIP know the uncompressed size
	    entry.setSize(1 + 4 + xmlBytes.length);  
	    out.putNextEntry(entry);

	    // 3) write boolean, length, then raw bytes
	    DataOutputStream dataOut = new DataOutputStream(out);
	    dataOut.writeBoolean(enable_auto_layout);
	    dataOut.writeInt(xmlBytes.length);
	    dataOut.write(xmlBytes);
	    dataOut.flush();          // push into the ZIP entry

	    // 4) close *only* the entry
	    out.closeEntry();
		
		
//		out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
//		
//		// Wrap only the entry in an ObjectOutputStream
//	    
//		 try (DataOutputStream dataOut = new DataOutputStream(out)) {
//		        dataOut.writeBoolean(enable_auto_layout);
//		        dataOut.writeUTF(model_xml);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		out.close();
	}

//	public static String exportBPMNDiagram(final BPMNDiagram diagram) throws Exception {
//		   
//		final UIContext context = new UIContext();
//		final UIPluginContext uiPluginContext = context.getMainPluginContext();
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					UIManager.setLookAndFeel(new MetalLookAndFeel());
//				} catch (UnsupportedLookAndFeelException e) {
//					throw new RuntimeException(e);
//				}
//			}
//		});
//		final BpmnDefinitions.BpmnDefinitionsBuilder definitionsBuilder = new BpmnDefinitions.BpmnDefinitionsBuilder(
//				(PluginContext) uiPluginContext, diagram);
//		final BpmnDefinitions definitions = new BpmnDefinitions("definitions", definitionsBuilder);
//		final StringBuilder sb = new StringBuilder();
//		sb.append(
//				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\"\n xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n targetNamespace=\"http://www.omg.org/bpmn20\"\n xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">");
//		sb.append(definitions.exportElements());
//		sb.append("</definitions>");
//		String result = sb.toString();
//		result = result.replaceAll("\n", "&#10;");
//		result = result.replaceAll(">&#10;", ">\n");
//		result = result.replaceAll("\"&#10;", "\"\n");
//		result = result.replaceFirst("<bpmndi:BPMNDiagram>.*</bpmndi:BPMNDiagram>", "");
//		result = result.replaceAll("<[a-zA-Z]+:[a-zA-Z]+/>", "");
//		
//		
//		List<String> tags = Arrays.asList("task", "endEvent", "startEvent"); 
//		
//		return result;
//	}
	
	public static String exportBPMNDiagram() throws Exception {		   
		return model_xml;
	}

	

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		
		System.out.println("Entered load");		
		final ZipEntry entry = in.getNextEntry();

		if (!ZIP_ENTRY_NAME.equals(entry.getName())) {
			throw new IOException("Failed to load BPMN port object. " + "Invalid zip entry name '" + entry.getName()
					+ "', expected '" + ZIP_ENTRY_NAME + "'.");
		}
		
		
		
		
		try {
			setSpec((BpmnPortObjectSpec) spec);
			List<Object> imported_data;
			imported_data = importBPMNDiagram(in);
			enable_auto_layout = (boolean) imported_data.get(0);
			model_xml = (String) imported_data.get(1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		in.closeEntry(); 
		
		
	}
	
	
	public static List<Object> importBPMNDiagram(InputStream inputStream) throws Exception {
		List<Object> res = null;
		
		
		DataInputStream dataIn = new DataInputStream(inputStream);
	    enable_auto_layout = dataIn.readBoolean();
	    int len = dataIn.readInt();
	    byte[] xmlBytes = new byte[len];
	    dataIn.readFully(xmlBytes);
	    model_xml = new String(xmlBytes, StandardCharsets.UTF_8);
		
//		try (DataInputStream dataIn = new DataInputStream(inputStream)) {
//	        enable_auto_layout = dataIn.readBoolean();
//	        model_xml = dataIn.readUTF();
//	        System.out.print("READ");
//	        System.out.print(model_xml);
//	        
//	    } catch (Exception e) {
//	    	System.out.print("FAILED");
//	    	System.out.print(e);
//	        e.printStackTrace();
//	    }
		
		res = List.of(enable_auto_layout, model_xml);
		return res;
	}


	public static class BpmnPortObjectSerializer
			extends AbstractPortObject.AbstractPortObjectSerializer<BpmnPortObject> {

	}

//	public static void exportBPMNDiagramToFile(OutputStream outStream) throws Exception {
//		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outStream));
//		bw.write(enable_auto_layout);
//		bw.write(model_xml);
//		bw.close();
//
//	}
	
	@Override
	public Map<String, List<?>> getJSON() {
	
		Map<String, List<?>> result = new HashMap<>();
		
		try {			
			String xmlOutput = model_xml;
			System.out.println(model_xml);
			String key = "xml"; 
			String key_2 = "layouter"; 
			result.put(key, Collections.singletonList(xmlOutput));
			result.put(key_2, Collections.singletonList(enable_auto_layout));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;	
		
	}
}
