package org.pm4knime.node.conversion.table2pn;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.node.DefaultModel;
import org.pm4knime.node.conversion.pn2table.PetriNetCell;
import org.pm4knime.node.conversion.pn2table.PetriNetValue;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.PetriNetUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;

final class Table2PetriNetConverterNodeModel {

    private Table2PetriNetConverterNodeModel() {
    }

    static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o)
        throws InvalidSettingsException {

        validateInputSpec(i.getInTableSpec(0));
        o.setOutSpec(0, new PetriNetPortObjectSpec());
    }

    static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o) {
        try {
            final BufferedDataTable inTable = i.getInTable(0);
            final PetriNetPortObject pnPO = createPortObject(inTable);
            o.setOutData(0, pnPO);
            o.setInternalData(pnPO);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void validateInputSpec(final DataTableSpec inSpec) throws InvalidSettingsException {
        final int columnIndex = findPetriNetColumnIndex(inSpec);
        if (columnIndex < 0) {
            throw new InvalidSettingsException("No Petri net column in input");
        }
        final DataColumnSpec columnSpec = inSpec.getColumnSpec(columnIndex);
        if (!columnSpec.getType().getCellClass().equals(PetriNetCell.class)) {
            throw new InvalidSettingsException(
                "Column \"" + columnSpec.getName() + "\" does not contain Petri nets");
        }
    }

    private static PetriNetPortObject createPortObject(final BufferedDataTable inTable) throws InvalidSettingsException {
        if (inTable.size() == 0) {
            throw new InvalidSettingsException("Input table is empty.");
        }

        final DataTableSpec inSpec = inTable.getDataTableSpec();
        final int columnIndex = findPetriNetColumnIndex(inSpec);
        if (columnIndex < 0) {
            throw new InvalidSettingsException("No Petri net column in input");
        }

        final RowIterator it = inTable.iterator();
        while (it.hasNext()) {
            final DataRow row = it.next();
            final DataCell cell = row.getCell(columnIndex);
            if (!cell.isMissing()) {
                final String stringPN = ((PetriNetValue)cell).getPetriNetString();
                final AcceptingPetriNet pn = PetriNetUtil.stringToPetriNet(stringPN);
                return new PetriNetPortObject(pn);
            }
        }

        throw new InvalidSettingsException("Input table contains only missing Petri net cells.");
    }

    private static int findPetriNetColumnIndex(final DataTableSpec spec) {
        for (int i = 0; i < spec.getNumColumns(); i++) {
            if (spec.getColumnSpec(i).getType().getCellClass().equals(PetriNetCell.class)) {
                return i;
            }
        }
        return -1;
    }
}
