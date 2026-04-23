package org.pm4knime.node.conformance.table.performance;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.RepResultPortObjectTable;

public class PerformanceCheckerNodeFactory extends DefaultNodeFactory {

    public PerformanceCheckerNodeFactory() {
        super(
            DefaultNode.create()
                .name("Alignment-Based Performance Evaluator")
                .icon("../../category-conformance.png")
                .shortDescription(
                    "Based on the replay result, this node computes the statistical performance information.")
                .fullDescription("This node computes the statistical precision information based on the result of "
                    + "alignment-based replayer.\r\n"
                    + "        Conceptually, the precision of a process model compared to one event log is supposed "
                    + "to be (1) high when the model \r\n"
                    + "        allows for few traces not seen in the log; and (2) low when it allows for many traces "
                    + "not seen in the log.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Replay Result", "replay result", RepResultPortObjectTable.TYPE)
                    .addOutputTable("Global Performance StatInfo",
                        "global performance statistical information.")
                    .addOutputTable("Transition Performance Statistics",
                        "performance statistical information for the different transitions in the Petri net "
                            + "(waiting time, synchronization time, and sojourn time).")
                    .addOutputTable("Place Performance Statistics",
                        "performance statistical information for the different places in the Petri net "
                            + "(waiting time, synchronization time, and sojourn time)."))
                .model(m -> m
                    .parametersClass(PerformanceCheckerNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new PerformanceCheckerNodeModel(PerformanceCheckerNodeSettings.class);
                        model.m_settings = i.getParameters();
                        o.setOutSpecs(model.configure(i.getInPortSpecs()));
                    })
                    .execute((i, o) -> {
                        final var model = new PerformanceCheckerNodeModel(PerformanceCheckerNodeSettings.class);
                        model.m_settings = i.getParameters();
                        try {
                            o.setOutData(model.execute(i.getInPortObjects(), i.getExecutionContext()));
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }))
                .nodeType(NodeType.Other));
    }
}
