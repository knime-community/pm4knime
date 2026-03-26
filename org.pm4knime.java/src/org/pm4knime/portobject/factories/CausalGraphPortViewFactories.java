package org.pm4knime.portobject.factories;

import org.knime.core.webui.node.port.PortViewFactory;
import org.pm4knime.portobject.CausalGraphPortObject;

@SuppressWarnings("restriction")
public final class CausalGraphPortViewFactories extends AbstractGraphPortViewFactories<CausalGraphPortObject> {

    private static final CausalGraphPortViewFactories INSTANCE = new CausalGraphPortViewFactories();

    static final PortViewFactory<CausalGraphPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    
    private static Class<?> portClass = CausalGraphPortObject.class;
	private static String portName = "Causal graph";


    public static void register() {

        register(portClass, portName, PORT_VIEW_FACTORY);
    }

    @Override
    protected Class<?> getBundleClass() {
        return CausalGraphPortViewFactories.class;
    }

    private CausalGraphPortViewFactories() {
    }
}