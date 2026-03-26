package org.pm4knime.portobject.factories;

import java.util.List;

import org.knime.core.webui.node.port.PortSpecViewFactory;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;

@SuppressWarnings("restriction")
public final class HybridPetriNetPortViewFactories extends AbstractGraphPortViewFactories<HybridPetriNetPortObject> {

    private static final HybridPetriNetPortViewFactories INSTANCE = new HybridPetriNetPortViewFactories();

    static final PortViewFactory<HybridPetriNetPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    static final PortSpecViewFactory<HybridPetriNetPortObjectSpec> PORT_SPEC_VIEW_FACTORY =
        spec -> INSTANCE.createDefaultSpecView("Hybrid Petri net port");

    public static void register() {
        final var portName = "Hybrid Petri net";
        PortViewManager.registerPortViews(
        		HybridPetriNetPortObject.class,
            List.of(
                new PortViewManager.PortViewDescriptor(portName, PORT_SPEC_VIEW_FACTORY),
                new PortViewManager.PortViewDescriptor(portName, PORT_VIEW_FACTORY)
            ),
            List.of(0),
            List.of(1)
        );
    }

    @Override
    protected Class<?> getBundleClass() {
        return HybridPetriNetPortViewFactories.class;
    }

    private HybridPetriNetPortViewFactories() {
    }
}