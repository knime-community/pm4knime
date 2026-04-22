package org.pm4knime.node.conversion.table2hpn;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.node.DefaultModel;
import org.pm4knime.node.conversion.hpn2table.HybridPetriNetCell;
import org.pm4knime.node.conversion.hpn2table.HybridPetriNetValue;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;
import org.pm4knime.util.HybridPetriNetUtil;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;

final class Table2HybridPetriNetConverterNodeModel {

    private Table2HybridPetriNetConverterNodeModel() {
    }

    static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o)
        throws InvalidSettingsException {

        validateInputSpec(i.getInTableSpec(0));
        o.setOutSpec(0, new HybridPetriNetPortObjectSpec());
    }

    static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o) {
        try {
            final BufferedDataTable inTable = i.getInTable(0);
            final HybridPetriNetPortObject pnPO = createPortObject(inTable);
            o.setOutData(0, pnPO);
            o.setInternalData(pnPO);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void validateInputSpec(final DataTableSpec inSpec) throws InvalidSettingsException {
        final int columnIndex = findHybridPetriNetColumnIndex(inSpec);
        if (columnIndex < 0) {
            throw new InvalidSettingsException("No Hybrid Petri net column in input");
        }
        final DataColumnSpec columnSpec = inSpec.getColumnSpec(columnIndex);
        if (!columnSpec.getType().getCellClass().equals(HybridPetriNetCell.class)) {
            throw new InvalidSettingsException(
                "Column \"" + columnSpec.getName() + "\" does not contain Hybrid Petri nets");
        }
    }

    private static HybridPetriNetPortObject createPortObject(final BufferedDataTable inTable)
        throws InvalidSettingsException {

        if (inTable.size() == 0) {
            throw new InvalidSettingsException("Input table is empty.");
        }

        final DataTableSpec inSpec = inTable.getDataTableSpec();
        final int columnIndex = findHybridPetriNetColumnIndex(inSpec);
        if (columnIndex < 0) {
            throw new InvalidSettingsException("No Hybrid Petri net column in input");
        }

        final RowIterator it = inTable.iterator();
        while (it.hasNext()) {
            final DataRow row = it.next();
            final DataCell cell = row.getCell(columnIndex);
            if (!cell.isMissing()) {
                final String stringPN = ((HybridPetriNetValue)cell).getHybridPetriNetString();
                final ExtendedHybridPetrinet pn = HybridPetriNetUtil.stringToHybridPetriNet(stringPN);
                return new HybridPetriNetPortObject(pn);
            }
        }

        throw new InvalidSettingsException("Input table contains only missing Hybrid Petri net cells.");
    }

    private static int findHybridPetriNetColumnIndex(final DataTableSpec spec) {
        for (int i = 0; i < spec.getNumColumns(); i++) {
            if (spec.getColumnSpec(i).getType().getCellClass().equals(HybridPetriNetCell.class)) {
                return i;
            }
        }
        return -1;
    }
}
