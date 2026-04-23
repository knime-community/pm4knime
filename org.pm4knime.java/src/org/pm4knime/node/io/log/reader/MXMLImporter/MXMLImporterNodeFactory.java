package org.pm4knime.node.io.log.reader.MXMLImporter;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.XLogPortObject;

public class MXMLImporterNodeFactory extends DefaultNodeFactory {

    public MXMLImporterNodeFactory() {
        super(
            DefaultNode.create()
                .name("MXML Reader")
                .icon("../../../read.png")
                .shortDescription("This node imports an event log from an MXML file.")
                .fullDescription("This node imports an event log from an MXML file using the Open Naive method.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p.addOutputPort("Event Log", "an event log", XLogPortObject.TYPE))
                .model(m -> m
                    .parametersClass(MXMLImporterNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new MXMLImporterNodeModel(MXMLImporterNodeSettings.class);
                        model.setSettings(i.getParameters());
                        o.setOutSpecs(model.configure(i.getInPortSpecs()));
                    })
                    .execute((i, o) -> {
                        final var model = new MXMLImporterNodeModel(MXMLImporterNodeSettings.class);
                        model.setSettings(i.getParameters());
                        try {
                            o.setOutData(model.execute(i.getInPortObjects(), i.getExecutionContext()));
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }))
                .nodeType(NodeType.Source));
    }
}
