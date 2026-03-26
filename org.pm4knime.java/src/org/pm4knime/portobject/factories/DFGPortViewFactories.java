package org.pm4knime.portobject.factories;

import java.util.List;

import org.knime.core.webui.node.port.PortSpecViewFactory;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.DfgMsdPortObjectSpec;

@SuppressWarnings("restriction")
public final class DFGPortViewFactories extends AbstractGraphPortViewFactories<DfgMsdPortObject> {

    private static final DFGPortViewFactories INSTANCE = new DFGPortViewFactories();

    static final PortViewFactory<DfgMsdPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    static final PortSpecViewFactory<DfgMsdPortObjectSpec> PORT_SPEC_VIEW_FACTORY =
        spec -> INSTANCE.createDefaultSpecView("Directly-Follows Graph (DFG) port");

    public static void register() {
        final var portName = "Directly-Follows Graph (DFG)";
        PortViewManager.registerPortViews(
        	DfgMsdPortObject.class,
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
        return DFGPortViewFactories.class;
    }

    private DFGPortViewFactories() {
    }
}