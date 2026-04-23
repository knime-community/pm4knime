package org.pm4knime.node.conversion.hpn2table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.HybridPetriNetPortObject;

public class HybridPetriNet2TableConverterNodeFactory extends DefaultNodeFactory {

    public HybridPetriNet2TableConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Hybrid Petri Net to Table")
                .icon("../category-conversion.png")
                .shortDescription("Convert a hybrid Petri net into a KNIME Data Table")
                .fullDescription("This node converts a hybrid Petri net into a KNIME data table.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Hybrid Petri net", "a hybrid Petri net", HybridPetriNetPortObject.TYPE)
                    .addOutputTable("Table", "an event table"))
                .model(m -> m
                    .parametersClass(HybridPetriNet2TableConverterNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new HybridPetriNet2TableConverterNodeModel(
                            HybridPetriNet2TableConverterNodeSettings.class);
                        model.m_settings = i.getParameters();
                        o.setOutSpecs(model.configure(i.getInPortSpecs()));
                    })
                    .execute((i, o) -> {
                        final var model = new HybridPetriNet2TableConverterNodeModel(
                            HybridPetriNet2TableConverterNodeSettings.class);
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
