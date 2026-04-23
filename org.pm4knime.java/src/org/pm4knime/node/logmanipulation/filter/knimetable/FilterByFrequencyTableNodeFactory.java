package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

public final class FilterByFrequencyTableNodeFactory extends DefaultNodeFactory {

    public FilterByFrequencyTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Filter Event Table by Frequency")
                .icon("../../category-manipulation.png")
                .shortDescription("This node filters the traces based on their frequency in the event table.")
                .fullDescription("This node filters the traces based on their frequency in the event table. \r\n"
                    + "        Traces are always sorted based on the frequencies of the corresponding trace variants, "
                    + "and the most frequent trace variants are kept/removed. \r\n"
                    + "        The filtering strategy is used to determine whether to keep or to remove the most "
                    + "frequent trace variants. \r\n"
                    + "        The percentage of trace variants to be kept/removed is determined by the two filtering "
                    + "options: filtering type and and filtering threshold.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Event Table", "The event table to be filtered.")
                    .addOutputTable("Filtered Event Table", "The filtered event table."))
                .model(m -> m
                    .parametersClass(FilterByFrequencyTableNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new FilterByFrequencyTableNodeModel(
                            FilterByFrequencyTableNodeSettings.class);
                        o.setOutSpecs(model.configureForDefaultNode(i.getInPortSpecs(), i.getParameters()));
                    })
                    .execute((i, o) -> {
                        final var model = new FilterByFrequencyTableNodeModel(
                            FilterByFrequencyTableNodeSettings.class);
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
