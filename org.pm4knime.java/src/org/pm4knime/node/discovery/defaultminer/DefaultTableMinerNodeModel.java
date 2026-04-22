package org.pm4knime.node.discovery.defaultminer;

import java.util.Arrays;
import java.util.OptionalInt;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.data.sort.RowComparator;
import org.knime.core.data.sort.RowComparator.ColumnComparatorBuilder;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.node.DefaultModel;
import org.pm4knime.portobject.AbstractJSONPortObject;

public abstract class DefaultTableMinerNodeModel<S extends DefaultTableMinerSettings> {

    protected BufferedDataTable logPO;
    protected AbstractJSONPortObject pmPO;
    protected S m_settings;

    protected DefaultTableMinerNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes,
        final String viewName, final Class<S> modelSettingsClass) {
    }

    public static <S extends DefaultTableMinerSettings, M extends DefaultTableMinerNodeModel<S>> void configure(
        final DefaultModel.ConfigureInput i, final DefaultModel.ConfigureOutput o, final M model)
        throws InvalidSettingsException {

        model.m_settings = i.getParameters();

        if (i.getInPortSpec(0) == null) {
            o.setOutSpec(0, null);
            return;
        }

        final DataTableSpec logSpec = i.getInTableSpec(0);
        if (model.m_settings.e_classifier == null || model.m_settings.t_classifier == null
            || model.m_settings.time_classifier == null) {
            throw new InvalidSettingsException("Classifiers are not set! Please open the dialog and configure the node!");
        }

        o.setOutSpecs(model.configureOutSpec(logSpec));
    }

    public static <S extends DefaultTableMinerSettings, M extends DefaultTableMinerNodeModel<S>> void execute(
        final DefaultModel.ExecuteInput i, final DefaultModel.ExecuteOutput o, final M model) {

        try {
            model.m_settings = i.getParameters();
            model.logPO = i.getInTable(0);

            final var sorter = new BufferedDataTableSorter(model.logPO,
                toRowComparator(model.logPO.getDataTableSpec(),
                    new String[]{model.m_settings.t_classifier, model.m_settings.time_classifier}));
            sorter.setSortInMemory(false);
            model.logPO = sorter.sort(i.getExecutionContext());

            model.pmPO = model.mine(model.logPO, i.getExecutionContext());

            o.setOutData(0, model.pmPO);
            o.setInternalData(model.pmPO);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static RowComparator toRowComparator(final DataTableSpec spec, final String[] sortingColumns) {
        final var rc = RowComparator.on(spec);
        Arrays.stream(sortingColumns).forEach(column -> {
            final var ascending = true;
            final var alphaNum = true;
            resolveColumnName(spec, column).ifPresentOrElse(
                col -> rc.thenComparingColumn(col,
                    c -> configureColumnComparatorBuilder(spec, ascending, alphaNum, col, c)),
                () -> rc.thenComparingRowKey(
                    k -> k.withDescendingSortOrder(!ascending).withAlphanumericComparison(alphaNum)));
        });
        return rc.build();
    }

    private static ColumnComparatorBuilder configureColumnComparatorBuilder(final DataTableSpec spec,
        final boolean ascending, final boolean alphaNum, final int col, final ColumnComparatorBuilder c) {

        var compBuilder = c.withDescendingSortOrder(!ascending);
        if (spec.getColumnSpec(col).getType().isCompatible(StringValue.class)) {
            compBuilder.withAlphanumericComparison(alphaNum);
        }
        return compBuilder.withMissingsLast(false);
    }

    private static OptionalInt resolveColumnName(final DataTableSpec dts, final String colName) {
        final var idx = dts.findColumnIndex(colName);
        if (idx == -1) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(idx);
    }

    protected abstract AbstractJSONPortObject mine(BufferedDataTable log, ExecutionContext exec) throws Exception;

    protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec);
}
