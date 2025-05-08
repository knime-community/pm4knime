package org.pm4knime.node.io.bpmn.reader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.AbstractJSONPortObject;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.portobject.BpmnPortObjectSpec;
import org.pm4knime.util.defaultnode.ReaderNodeModel;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;

public class BpmnReaderNodeModel extends ReaderNodeModel {

	protected BpmnPortObject bpmn_po;

	public BpmnReaderNodeModel(Class<ReaderNodeSettings> class1) {
		super(class1, new String[] { ".bpmn" }, new BpmnPortObjectSpec(), new PortType[] { BpmnPortObject.TYPE },
				"BPMN JS View");
	}

	@Override
	protected AbstractJSONPortObject write_file_from_stream(InputStream inputStream) {

		bpmn_po = new BpmnPortObject();
		
		int nRead;
        byte[] data = new byte[1024];
        try {
        	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			    buffer.write(data, 0, nRead);
			}
			
			String xmlContent = buffer.toString(StandardCharsets.UTF_8);
			buffer.flush();
	        bpmn_po = new BpmnPortObject(xmlContent);

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
        
                bpmn_po.disable_auto_layout();

		return bpmn_po;
	}

	@Override
	protected PortObjectSpec[] configureOutSpec() {
		return new PortObjectSpec[] { new BpmnPortObjectSpec() };
	}

}
