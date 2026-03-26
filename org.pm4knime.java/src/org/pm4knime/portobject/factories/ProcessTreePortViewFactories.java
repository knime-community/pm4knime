package org.pm4knime.portobject.factories;

import org.knime.core.webui.node.port.PortViewFactory;
import org.pm4knime.portobject.ProcessTreePortObject;

@SuppressWarnings("restriction")
public final class ProcessTreePortViewFactories extends AbstractGraphPortViewFactories<ProcessTreePortObject> {

    private static final ProcessTreePortViewFactories INSTANCE = new ProcessTreePortViewFactories();

    static final PortViewFactory<ProcessTreePortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);
    
    private static Class<?> portClass = ProcessTreePortObject.class;
	private static String portName = "Process tree";

	
    public static void register() {

        register(portClass, portName, PORT_VIEW_FACTORY);
    }

    @Override
    protected Class<?> getBundleClass() {
        return ProcessTreePortViewFactories.class;
    }

    private ProcessTreePortViewFactories() {
    }
}