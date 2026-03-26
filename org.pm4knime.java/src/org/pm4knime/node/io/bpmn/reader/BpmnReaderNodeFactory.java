package org.pm4knime.node.io.bpmn.reader;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.util.defaultnode.ReaderNodeModel;
import org.pm4knime.util.defaultnode.ReaderNodeSettings;

public class BpmnReaderNodeFactory extends DefaultNodeFactory {

    public BpmnReaderNodeFactory() {
        super(
            DefaultNode.create()
                .name("BPMN Reader")
                .icon("../../read.png")
                .shortDescription("Import a BPMN model.")
                .fullDescription(
                    "This node imports a BPMN model from a BPMN file. BPMN, or Business Process Model and Notation, "
                        + "encompasses several key elements that collectively define and illustrate a business process.")
                .sinceVersion(3, 0, 0)
                .ports(p -> p.addOutputPort("BPMN", "a BPMN model", BpmnPortObject.TYPE))
                .model(m -> m
                    .parametersClass(ReaderNodeSettings.class)
                    .configure((i, o) -> ReaderNodeModel.configure(i, o,
                        new BpmnReaderNodeModel(ReaderNodeSettings.class)))
                    .execute((i, o) -> ReaderNodeModel.execute(i, o,
                        new BpmnReaderNodeModel(ReaderNodeSettings.class))))
//                .addView(v -> ModernViews.bpmn(v, BpmnReaderNodeFactory.class, "BPMN view"))
                .nodeType(NodeType.Source));
    }
}
