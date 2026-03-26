package org.pm4knime.portobject.factories;

import org.knime.core.webui.node.port.PortViewFactory;
import org.pm4knime.portobject.BpmnPortObject;


@SuppressWarnings("restriction")
public final class BPMNPortViewFactories extends AbstractGraphPortViewFactories<BpmnPortObject> {

    private static final BPMNPortViewFactories INSTANCE = new BPMNPortViewFactories();

    static final PortViewFactory<BpmnPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

	private static Class<?> portClass = BpmnPortObject.class;
	private static String portName = "BPMN View";


    public static void register() {

        register(portClass, portName, PORT_VIEW_FACTORY);
    }

	@Override
    protected Class<?> getBundleClass() {
        return BPMNPortViewFactories.class;
    }

    private BPMNPortViewFactories() {
    }
}