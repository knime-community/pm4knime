package org.pm4knime.node.conformance.table.fitness;

import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.node.DefaultModel;
import org.pm4knime.portobject.RepResultPortObjectSpecTable;
import org.pm4knime.portobject.RepResultPortObjectTable;
import org.pm4knime.util.ReplayerUtil;

@SuppressWarnings("restriction")
final class FitnessCheckerNodeModel {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(FitnessCheckerNodeModel.class);

    private FitnessCheckerNodeModel() {
    }

    static void configure(final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o)
        throws InvalidSettingsException {

        if (!(i.getInPortSpec(0) instanceof RepResultPortObjectSpecTable)) {
            throw new InvalidSettingsException("Input is not a valid replay result!");
        }

        o.setOutSpec(0, createFitnessTableSpec());
    }

    static void execute(final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o) {
        try {
            LOGGER.info("Start: Unified PNReplayer Conformance Checking");
            final RepResultPortObjectTable repResultPO = (RepResultPortObjectTable)i.getInPortObject(0);

            ReplayerUtil.adjustRepResult(repResultPO.getRepResult(), repResultPO.getNet());

            final BufferedDataContainer buf = i.getExecutionContext().createDataContainer(createFitnessTableSpec());
            final Map<String, Object> info = repResultPO.getRepResult().getInfo();

            int rowIndex = 0;
            for (final Map.Entry<String, Object> entry : info.entrySet()) {
                final Object value = entry.getValue();
                if (!(value instanceof Double)) {
                    continue;
                }

                final DataCell[] currentRow = new DataCell[] {
                    new StringCell(entry.getKey()),
                    new DoubleCell((Double)value)
                };
                buf.addRowToTable(new DefaultRow(Integer.toString(rowIndex++), currentRow));
            }

            buf.close();
            final BufferedDataTable bt = buf.getTable();
            o.setOutData(0, bt);
            o.setInternalData(bt);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static DataTableSpec createFitnessTableSpec() {
        final DataColumnSpec[] cSpec = new DataColumnSpec[2];
        cSpec[0] = new DataColumnSpecCreator("Type", StringCell.TYPE).createSpec();
        cSpec[1] = new DataColumnSpecCreator("Value", DoubleCell.TYPE).createSpec();
        return new DataTableSpec("Fitness Statistic", cSpec);
    }
}
