package org.pm4knime.portobject.factories;

import java.util.List;

import org.knime.core.webui.node.port.PortSpecViewFactory;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;

@SuppressWarnings("restriction")
public final class ProcessTreePortViewFactories extends AbstractGraphPortViewFactories<ProcessTreePortObject> {

    private static final ProcessTreePortViewFactories INSTANCE = new ProcessTreePortViewFactories();

    static final PortViewFactory<ProcessTreePortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    static final PortSpecViewFactory<ProcessTreePortObjectSpec> PORT_SPEC_VIEW_FACTORY =
        spec -> INSTANCE.createDefaultSpecView("Process tree port");

    public static void register() {
        final var portName = "Process tree";
        PortViewManager.registerPortViews(
            ProcessTreePortObject.class,
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
        return ProcessTreePortViewFactories.class;
    }

    private ProcessTreePortViewFactories() {
    }
}