package org.pm4knime.node.conversion.table2log;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.XLogPortObject;

public class Table2XLogConverterNodeFactory extends DefaultNodeFactory {

    public Table2XLogConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Table to Event Log")
                .icon("../category-conversion.png")
                .shortDescription("This node converts a KNIME Data Table into an event log.")
                .fullDescription("This node converts a KNIME Data Table into an event log.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Table", "an event table")
                    .addOutputPort("Event Log", "an event log", XLogPortObject.TYPE))
                .model(m -> m
                    .parametersClass(Table2XLogConverterNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new Table2XLogConverterNodeModel(Table2XLogConverterNodeSettings.class);
                        model.m_settings = i.getParameters();
                        o.setOutSpecs(model.configure(i.getInPortSpecs()));
                    })
                    .execute((i, o) -> {
                        final var model = new Table2XLogConverterNodeModel(Table2XLogConverterNodeSettings.class);
                        model.m_settings = i.getParameters();
                        try {
                            o.setOutData(model.execute(i.getInPortObjects(), i.getExecutionContext()));
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }))
                .nodeType(NodeType.Manipulator));
    }
}
