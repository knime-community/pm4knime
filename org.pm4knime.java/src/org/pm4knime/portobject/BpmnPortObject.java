package org.pm4knime.portobject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        // Saves the *current static* values

        ZipEntry entry = new ZipEntry(ZIP_ENTRY_NAME);
        out.putNextEntry(entry);

        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeBoolean(BpmnPortObject.enable_auto_layout); // Save static field
        if (BpmnPortObject.model_xml != null) {
            byte[] xmlBytes = BpmnPortObject.model_xml.getBytes(StandardCharsets.UTF_8);
            dataOut.writeInt(xmlBytes.length);
            dataOut.write(xmlBytes);
        } else {
            dataOut.writeInt(-1); // Convention for null string
        }
        dataOut.flush();
        out.closeEntry();
    }
	
	public static String exportBPMNDiagram() throws Exception {		   
		return model_xml;
	}
	

	@Override
    protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
     
        System.out.println("BpmnPortObject: Entered load (will update static fields)");
        final ZipEntry entry = in.getNextEntry();
        if (entry == null) {
            throw new IOException("Failed to load BPMN port object. No zip entry found.");
        }
        if (!ZIP_ENTRY_NAME.equals(entry.getName())) {
            throw new IOException("Failed to load BPMN port object. Invalid zip entry name '" + entry.getName()
                                  + "', expected '" + ZIP_ENTRY_NAME + "'.");
        }

        this.setSpec(spec); 

        DataInputStream dataIn = new DataInputStream(in);

        BpmnPortObject.enable_auto_layout = dataIn.readBoolean();
        int len = dataIn.readInt();
        if (len == -1) {
            BpmnPortObject.model_xml = null;
        } else if (len < 0) {
            throw new IOException("Invalid length for BPMN XML: " + len);
        } else if (len == 0) {
            BpmnPortObject.model_xml = "";
        }
        else {
            byte[] xmlBytes = new byte[len];
            dataIn.readFully(xmlBytes); 
            BpmnPortObject.model_xml = new String(xmlBytes, StandardCharsets.UTF_8);
        }
        in.closeEntry();
        System.out.println("BpmnPortObject: Load successful (static fields updated).");
    }
	
	
	public static List<Object> importBPMNDiagram(InputStream inputStream) throws Exception {
		List<Object> res = null;
		
		
		DataInputStream dataIn = new DataInputStream(inputStream);
	    enable_auto_layout = dataIn.readBoolean();
	    int len = dataIn.readInt();
	    byte[] xmlBytes = new byte[len];
	    dataIn.readFully(xmlBytes);
	    model_xml = new String(xmlBytes, StandardCharsets.UTF_8);
		
		
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
