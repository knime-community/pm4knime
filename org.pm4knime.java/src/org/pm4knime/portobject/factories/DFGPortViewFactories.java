package org.pm4knime.portobject.factories;

import org.knime.core.webui.node.port.PortViewFactory;
import org.pm4knime.portobject.DfgMsdPortObject;

@SuppressWarnings("restriction")
public final class DFGPortViewFactories extends AbstractGraphPortViewFactories<DfgMsdPortObject> {

    private static final DFGPortViewFactories INSTANCE = new DFGPortViewFactories();

    static final PortViewFactory<DfgMsdPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    
    private static Class<?> portClass = DfgMsdPortObject.class;
	private static String portName = "DFG View";


    public static void register() {

        register(portClass, portName, PORT_VIEW_FACTORY);
    }

    private DFGPortViewFactories() {
    }
}