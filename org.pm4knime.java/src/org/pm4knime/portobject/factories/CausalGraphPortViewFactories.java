package org.pm4knime.portobject.factories;

import java.util.List;

import org.knime.core.webui.node.port.PortSpecViewFactory;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.CausalGraphPortObjectSpec;

@SuppressWarnings("restriction")
public final class CausalGraphPortViewFactories extends AbstractGraphPortViewFactories<CausalGraphPortObject> {

    private static final CausalGraphPortViewFactories INSTANCE = new CausalGraphPortViewFactories();

    static final PortViewFactory<CausalGraphPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    static final PortSpecViewFactory<CausalGraphPortObjectSpec> PORT_SPEC_VIEW_FACTORY =
        spec -> INSTANCE.createDefaultSpecView("Causal graph port");

    public static void register() {
        final var portName = "Causal graph";
        PortViewManager.registerPortViews(
            CausalGraphPortObject.class,
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
        return CausalGraphPortViewFactories.class;
    }

    private CausalGraphPortViewFactories() {
    }
}