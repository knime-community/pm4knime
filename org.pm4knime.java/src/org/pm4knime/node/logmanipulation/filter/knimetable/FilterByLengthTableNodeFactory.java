package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

public final class FilterByLengthTableNodeFactory extends DefaultNodeFactory {

    public FilterByLengthTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Filter Event Table by Length")
                .icon("../../category-manipulation.png")
                .shortDescription("This node filters the traces based on their length.")
                .fullDescription("This node filters the traces based on their length.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Event Table", "The event table to be filtered.")
                    .addOutputTable("Filtered Event Table", "The filtered event table."))
                .model(m -> m
                    .parametersClass(FilterByLengthTableNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new FilterByLengthTableNodeModel(FilterByLengthTableNodeSettings.class);
                        o.setOutSpecs(model.configureForDefaultNode(i.getInPortSpecs(), i.getParameters()));
                    })
                    .execute((i, o) -> {
                        final var model = new FilterByLengthTableNodeModel(FilterByLengthTableNodeSettings.class);
                        try {
                            o.setOutData(model.executeForDefaultNode(i.getInPortObjects(), i.getExecutionContext(),
                                i.getParameters()));
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }))
                .nodeType(NodeType.Manipulator));
    }
}
