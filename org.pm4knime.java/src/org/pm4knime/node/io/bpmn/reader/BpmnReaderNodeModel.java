package org.pm4knime.node.io.bpmn.reader;

import java.io.InputStream;
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
		

		try {
			List<Object> imported_data = BpmnPortObject.importBPMNDiagram(inputStream);
			String model_xml = (String) imported_data.get(1);
			bpmn_po = new BpmnPortObject(model_xml);
//			boolean enable_auto_layout = (boolean) imported_data.get(0);
			bpmn_po.disable_auto_layout();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bpmn_po;
	}

	@Override
	protected PortObjectSpec[] configureOutSpec() {
		return new PortObjectSpec[] { new BpmnPortObjectSpec() };
	}

}
