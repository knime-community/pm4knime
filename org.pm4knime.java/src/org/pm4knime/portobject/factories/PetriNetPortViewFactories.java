package org.pm4knime.portobject.factories;

import org.knime.core.webui.node.port.PortViewFactory;
import org.pm4knime.portobject.PetriNetPortObject;

@SuppressWarnings("restriction")
public final class PetriNetPortViewFactories extends AbstractGraphPortViewFactories<PetriNetPortObject> {

    private static final PetriNetPortViewFactories INSTANCE = new PetriNetPortViewFactories();

    static final PortViewFactory<PetriNetPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    
    private static Class<?> portClass = PetriNetPortObject.class;
	private static String portName = "Petri Net View";


    public static void register() {

        register(portClass, portName, PORT_VIEW_FACTORY);
    }

    @Override
    protected Class<?> getBundleClass() {
        return PetriNetPortViewFactories.class;
    }

    private PetriNetPortViewFactories() {
    }
}