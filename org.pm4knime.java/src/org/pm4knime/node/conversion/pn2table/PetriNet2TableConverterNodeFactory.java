package org.pm4knime.node.conversion.pn2table;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.portobject.PetriNetPortObject;

public class PetriNet2TableConverterNodeFactory extends DefaultNodeFactory {

    public PetriNet2TableConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Petri Net to Table")
                .icon("../category-conversion.png")
                .shortDescription("Convert a Petri net into a KNIME Data Table")
                .fullDescription("This node converts a Petri net into a KNIME data table.")
                .sinceVersion(2, 0, 0)
                .ports(p -> p
                    .addInputPort("Petri net", "a Petri net", PetriNetPortObject.TYPE)
                    .addOutputTable("Table", "an event table"))
                .model(m -> m
                    .parametersClass(PetriNet2TableConverterNodeSettings.class)
                    .configure((i, o) -> {
                        final var model = new PetriNet2TableConverterNodeModel(
                            PetriNet2TableConverterNodeSettings.class);
                        model.m_settings = i.getParameters();
                        o.setOutSpecs(model.configure(i.getInPortSpecs()));
                    })
                    .execute((i, o) -> {
                        final var model = new PetriNet2TableConverterNodeModel(
                            PetriNet2TableConverterNodeSettings.class);
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
