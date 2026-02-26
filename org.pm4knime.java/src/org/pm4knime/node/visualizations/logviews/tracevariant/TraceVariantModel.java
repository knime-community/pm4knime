package org.pm4knime.node.visualizations.logviews.tracevariant;

import java.util.*;

import org.knime.core.data.*;
import org.knime.core.data.def.*;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.*;
import org.knime.node.DefaultModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputColumnHandlingMode;
import org.pm4knime.util.defaultnode.TraceVariant;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;

final class TraceVariantModel {

    private static final String DELIMITER = " → ";

    // =========================================================
    // CONFIGURE
    // =========================================================

    static void configure(DefaultModel.ConfigureInput i,
                          DefaultModel.ConfigureOutput o)
            throws InvalidSettingsException {

        TraceVariantVisNodeSettings settings =
                (TraceVariantVisNodeSettings) i.getParameters();

        DataTableSpec inSpec =
                (DataTableSpec) i.getInPortSpec(0);

        if (settings.t_classifier == null ||
            settings.e_classifier == null ||
            settings.time_classifier == null) {
            throw new InvalidSettingsException("Classifiers are not set!");
        }

        if (settings.variantIdColumnName == null ||
            settings.variantIdColumnName.isBlank()) {
            throw new InvalidSettingsException("Variant ID column name is not set.");
        }

        if (inSpec.containsName(settings.variantIdColumnName)) {
            if (settings.existingVariantIdColumnMode
                == ExistingOutputColumnHandlingMode.FAIL) {

                throw new InvalidSettingsException(
                        "Column '" + settings.variantIdColumnName + "' already exists.");
            }

            o.setWarningMessage(
                    "Existing column '" + settings.variantIdColumnName +
                    "' will be overwritten.");
        }

        o.setOutSpec(0, createVariantTableSpec(settings));
        o.setOutSpec(1, createLogWithVariantSpec(inSpec, settings));
    }

    // =========================================================
    // EXECUTE
    // =========================================================

    static void execute(DefaultModel.ExecuteInput i,
                        DefaultModel.ExecuteOutput o) {

        try {
            TraceVariantVisNodeSettings settings =
                    (TraceVariantVisNodeSettings) i.getParameters();

            BufferedDataTable table =
                    (BufferedDataTable) i.getInPortObject(0);

            // ---- SORT (required) ----
            var sorter = new BufferedDataTableSorter(
                    table,
                    DefaultTableMinerNodeModel.toRowComparator(
                            table.getSpec(),
                            new String[]{
                                    settings.t_classifier,
                                    settings.time_classifier
                            }
                    )
            );

            sorter.setSortInMemory(false);
            table = sorter.sort(i.getExecutionContext());

            // ---- VARIANT REPRESENTATION ----
            TraceVariantRepresentation variants =
                    new TraceVariantRepresentation(
                            table,
                            settings.t_classifier,
                            settings.e_classifier
                    );

            // Map: sequenceHash -> Variant_X
            Map<Long, String> sequenceHashToVariant =
                    buildVariantHashMap(variants);

            // Map: traceId -> Variant_X
            Map<String, String> traceToVariant =
                    buildTraceToVariantMappingStreaming(
                            table,
                            sequenceHashToVariant,
                            settings
                    );

            var summary =
                    createVariantSummaryTable(
                            i.getExecutionContext(),
                            variants,
                            settings
                    );

            var logWithVariant =
                    createLogWithVariantTableOptimized(
                            i.getExecutionContext(),
                            table,
                            traceToVariant,
                            settings
                    );

            o.setOutData(0, summary);
            o.setOutData(1, logWithVariant);
            o.setInternalData(summary);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================
    // HASH-BASED VARIANT MAPPING
    // =========================================================

    private static Map<Long, String> buildVariantHashMap(
            TraceVariantRepresentation variants) {

        Map<Long, String> map = new HashMap<>();

        var list = variants.getVariants();

        for (int i = 0; i < list.size(); i++) {
            TraceVariant v = list.get(i);
            long hash = computeSequenceHash(v.getActivities());
            map.put(hash, "Variant_" + (i + 1));
        }

        return map;
    }

    private static Map<String, String> buildTraceToVariantMappingStreaming(
            BufferedDataTable table,
            Map<Long, String> sequenceHashToVariant,
            TraceVariantVisNodeSettings settings) {

        Map<String, String> result = new HashMap<>();

        DataTableSpec spec = table.getSpec();
        int traceIdx = spec.findColumnIndex(settings.t_classifier);
        int eventIdx = spec.findColumnIndex(settings.e_classifier);

        String currentTrace = null;
        long currentHash = 1L;

        for (DataRow row : table) {

            String traceId = row.getCell(traceIdx).toString();
            String activity = row.getCell(eventIdx).toString();

            if (!traceId.equals(currentTrace)) {

                if (currentTrace != null) {
                    result.put(currentTrace,
                            sequenceHashToVariant.getOrDefault(currentHash, "UNKNOWN"));
                }

                currentTrace = traceId;
                currentHash = 1L;
            }

            currentHash = 31 * currentHash + activity.hashCode();
        }

        // finalize last trace
        if (currentTrace != null) {
            result.put(currentTrace,
                    sequenceHashToVariant.getOrDefault(currentHash, "UNKNOWN"));
        }

        return result;
    }

    private static long computeSequenceHash(List<String> activities) {
        long hash = 1L;
        for (String act : activities) {
            hash = 31 * hash + act.hashCode();
        }
        return hash;
    }

    // =========================================================
    // OUTPUT TABLES
    // =========================================================

    private static BufferedDataTable createVariantSummaryTable(
            ExecutionContext exec,
            TraceVariantRepresentation variants,
            TraceVariantVisNodeSettings settings) {

        BufferedDataContainer container =
                exec.createDataContainer(createVariantTableSpec(settings));

        var list = variants.getVariants();

        for (int i = 0; i < list.size(); i++) {

            TraceVariant v = list.get(i);

            container.addRowToTable(new DefaultRow(
                    RowKey.createRowKey((long) i),
                    new StringCell("Variant_" + (i + 1)),
                    new IntCell(v.getFrequency()),
                    new StringCell(String.join(DELIMITER, v.getActivities()))
            ));
        }

        container.close();
        return container.getTable();
    }

    private static BufferedDataTable createLogWithVariantTableOptimized(
            ExecutionContext exec,
            BufferedDataTable table,
            Map<String, String> traceToVariant,
            TraceVariantVisNodeSettings settings) {

        DataTableSpec newSpec =
                createLogWithVariantSpec(table.getSpec(), settings);

        BufferedDataContainer container =
                exec.createDataContainer(newSpec);

        DataTableSpec spec = table.getSpec();
        int traceIdx = spec.findColumnIndex(settings.t_classifier);

        for (DataRow row : table) {

            int n = row.getNumCells();
            DataCell[] cells = new DataCell[n + 1];

            for (int c = 0; c < n; c++) {
                cells[c] = row.getCell(c);
            }

            String traceId = row.getCell(traceIdx).toString();

            cells[n] = new StringCell(
                    traceToVariant.getOrDefault(traceId, "UNKNOWN")
            );

            container.addRowToTable(new DefaultRow(row.getKey(), cells));
        }

        container.close();
        return container.getTable();
    }

    // =========================================================
    // SPECS
    // =========================================================

    private static DataTableSpec createVariantTableSpec(
            TraceVariantVisNodeSettings settings) {

        return new DataTableSpec(
                new DataColumnSpecCreator(
                        settings.variantIdColumnName,
                        StringCell.TYPE
                ).createSpec(),
                new DataColumnSpecCreator(
                        "Frequency",
                        IntCell.TYPE
                ).createSpec(),
                new DataColumnSpecCreator(
                        "Activity Sequence",
                        StringCell.TYPE
                ).createSpec()
        );
    }

    private static DataTableSpec createLogWithVariantSpec(
            DataTableSpec originalSpec,
            TraceVariantVisNodeSettings settings) {

        List<DataColumnSpec> cols = new ArrayList<>(originalSpec.getNumColumns());

        for (DataColumnSpec col : originalSpec) {
            if (!col.getName().equals(settings.variantIdColumnName)) {
                cols.add(col);
            }
        }

        cols.add(new DataColumnSpecCreator(
                settings.variantIdColumnName,
                StringCell.TYPE
        ).createSpec());

        return new DataTableSpec(
                cols.toArray(new DataColumnSpec[0])
        );
    }
}