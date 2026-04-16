package org.pm4knime.portobject.factories;

import org.knime.core.webui.node.port.PortViewFactory;
import org.pm4knime.portobject.HybridPetriNetPortObject;

@SuppressWarnings("restriction")
public final class HybridPetriNetPortViewFactories extends AbstractGraphPortViewFactories<HybridPetriNetPortObject> {

    private static final HybridPetriNetPortViewFactories INSTANCE = new HybridPetriNetPortViewFactories();

    static final PortViewFactory<HybridPetriNetPortObject> PORT_VIEW_FACTORY =
        obj -> INSTANCE.createPortObjectView(obj);

    
    private static Class<?> portClass = HybridPetriNetPortObject.class;
	private static String portName = "Hybrid Petri Net View";


    public static void register() {

        register(portClass, portName, PORT_VIEW_FACTORY);
    }

    @Override
    protected Class<?> getBundleClass() {
        return HybridPetriNetPortViewFactories.class;
    }

    private HybridPetriNetPortViewFactories() {
    }
}