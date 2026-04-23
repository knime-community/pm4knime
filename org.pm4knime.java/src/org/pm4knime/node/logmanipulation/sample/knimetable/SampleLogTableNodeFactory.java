package org.pm4knime.node.logmanipulation.sample.knimetable;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

public final class SampleLogTableNodeFactory extends DefaultNodeFactory {

    public SampleLogTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Event Table Partitioning")
                .icon("../../category-manipulation.png")
                .shortDescription("This node is used to randomly sample a percentage of traces from the event table.")
                .fullDescription("This node is used to randomly sample a percentage of traces from the event table. "
                    + "After sampling, it outputs two event tables: the sampled event table and the event table that "
                    + "contains the removed traces.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("Event Table", "The event table to be sampled.")
                    .addOutputTable("Sampled Event Table", "The sampled event table.")
                    .addOutputTable("Removed Traces Table", "The event table that contains the removed traces."))
                .model(m -> m
                    .parametersClass(SampleLogTableNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new SampleLogTableNodeModel(SampleLogTableNodeSettings.class);
                        o.setOutSpecs(model.configureForDefaultNode(i.getInPortSpecs(), i.getParameters()));
                    })
                    .execute((i, o) -> {
                        final var model = new SampleLogTableNodeModel(SampleLogTableNodeSettings.class);
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
