package org.pm4knime.portobject.factories;

import java.util.List;

import org.knime.core.webui.node.port.PortSpecViewFactory;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.portobject.BpmnPortObjectSpec;


@SuppressWarnings("restriction")
public final class BPMNPortViewFactories extends AbstractGraphPortViewFactories<BpmnPortObject> {

    private static final BPMNPortViewFactories INSTANCE = new BPMNPortViewFactories();

    static final PortViewFactory<BpmnPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    static final PortSpecViewFactory<BpmnPortObjectSpec> PORT_SPEC_VIEW_FACTORY =
        spec -> INSTANCE.createDefaultSpecView("BPMN port");

    public static void register() {
        final var portName = "BPMN";
        PortViewManager.registerPortViews(
        		BpmnPortObject.class,
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
        return BPMNPortViewFactories.class;
    }

    private BPMNPortViewFactories() {
    }
}