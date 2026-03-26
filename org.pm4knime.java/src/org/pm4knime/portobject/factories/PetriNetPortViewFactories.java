package org.pm4knime.portobject.factories;

import java.util.List;

import org.knime.core.webui.node.port.PortSpecViewFactory;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;

@SuppressWarnings("restriction")
public final class PetriNetPortViewFactories extends AbstractGraphPortViewFactories<PetriNetPortObject> {

    private static final PetriNetPortViewFactories INSTANCE = new PetriNetPortViewFactories();

    static final PortViewFactory<PetriNetPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    static final PortSpecViewFactory<PetriNetPortObjectSpec> PORT_SPEC_VIEW_FACTORY =
        spec -> INSTANCE.createDefaultSpecView("Petri net port");

    public static void register() {
        final var portName = "Petri net";
        PortViewManager.registerPortViews(
            PetriNetPortObject.class,
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
        return PetriNetPortViewFactories.class;
    }

    private PetriNetPortViewFactories() {
    }
}