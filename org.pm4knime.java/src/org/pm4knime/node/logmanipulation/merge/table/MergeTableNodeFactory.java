package org.pm4knime.node.logmanipulation.merge.table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

public final class MergeTableNodeFactory extends DefaultNodeFactory {

    public MergeTableNodeFactory() {
        super(
            DefaultNode.create()
                .name("Merge Event Tables")
                .icon("../../category-manipulation.png")
                .shortDescription("This node merges two event tables.")
                .fullDescription("This node merges two event tables.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputTable("First Event Table",
                        "The first event table to be merged (used as the base event table).")
                    .addInputTable("Second Event Table", "The second event table to be merged.")
                    .addOutputTable("Merged Event Table", "The merged event table."))
                .model(m -> m
                    .parametersClass(MergeTableNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new MergeTableNodeModel();
                        o.setOutSpecs(model.configure(i.getInPortSpecs(), i.getParameters()));
                    })
                    .execute((i, o) -> {
                        final var model = new MergeTableNodeModel();
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
