package org.pm4knime.node.conversion.pt2pn;

import org.knime.core.node.InvalidSettingsException;
import org.knime.node.DefaultModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.processtree.ProcessTree;

public class PT2PNConverterNodeModel {

    public PT2PNConverterNodeModel(final Class<?> modelSettingsClass) {
    }

    public static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o)
        throws InvalidSettingsException {

        if (!(i.getInPortSpec(0) instanceof ProcessTreePortObjectSpec)) {
            throw new InvalidSettingsException("Input is not a valid process tree!");
        }

        o.setOutSpec(0, new PetriNetPortObjectSpec());
    }

    public static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o) {
        try {
            final ProcessTreePortObject ptPO = (ProcessTreePortObject)i.getInPortObject(0);
            final ProcessTree tree = ptPO.getTree();

            final ProcessTree2Petrinet converter = new ProcessTree2Petrinet();
            final ProcessTree2Petrinet.PetrinetWithMarkings pn = converter.convert(tree, false);

            final AcceptingPetriNet anet = AcceptingPetriNetFactory.createAcceptingPetriNet(
                pn.petrinet, pn.initialMarking, pn.finalMarking);

            final PetriNetPortObject pnPO = new PetriNetPortObject(anet);
            o.setOutData(0, pnPO);
            o.setInternalData(pnPO);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
