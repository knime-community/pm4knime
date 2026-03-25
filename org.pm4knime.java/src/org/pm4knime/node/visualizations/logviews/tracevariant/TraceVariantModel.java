package org.pm4knime.node.visualizations.logviews.tracevariant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.node.DefaultModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerNodeModel;
import org.pm4knime.util.NodeSettingsUtils.ExistingOutputColumnHandlingMode;
import org.pm4knime.util.defaultnode.TraceVariant;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;

final class TraceVariantModel {

    private static final long PROGRESS_UPDATE_INTERVAL = 4_096L;
    private static final String DELIMITER = " \u2192 ";

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

        if (settings.t_classifier == null
                || settings.e_classifier == null
                || settings.time_classifier == null) {
            throw new InvalidSettingsException("Classifiers are not set!");
        }

        if (settings.variantIdColumnName == null
                || settings.variantIdColumnName.isBlank()) {
            throw new InvalidSettingsException("Variant ID column name is not set.");
        }

        if (inSpec.containsName(settings.variantIdColumnName)) {
            if (settings.existingVariantIdColumnMode
                    == ExistingOutputColumnHandlingMode.FAIL) {

                throw new InvalidSettingsException(
                        "Column '" + settings.variantIdColumnName + "' already exists.");
            }

            o.setWarningMessage(
                    "Existing column '" + settings.variantIdColumnName
                            + "' will be overwritten.");
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

            ExecutionContext exec = i.getExecutionContext();
            BufferedDataTable table =
                    (BufferedDataTable) i.getInPortObject(0);

            exec.setMessage("Sorting events by trace and time");

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
            table = sorter.sort(exec.createSubExecutionContext(0.35));

            exec.setMessage("Building trace variants");

            TraceVariantRepresentation variants =
                    new TraceVariantRepresentation(
                            table,
                            settings.t_classifier,
                            settings.e_classifier,
                            exec.createSubExecutionContext(0.25)
                    );

            Map<Long, String> sequenceHashToVariant =
                    buildVariantHashMap(variants);

            exec.setMessage("Assigning variant IDs to traces");

            Map<String, String> traceToVariant =
                    buildTraceToVariantMappingStreaming(
                            exec.createSubExecutionContext(0.15),
                            table,
                            sequenceHashToVariant,
                            settings
                    );

            exec.setMessage("Creating variant summary table");

            BufferedDataTable summary =
                    createVariantSummaryTable(
                            exec.createSubExecutionContext(0.05),
                            variants,
                            settings
                    );

            exec.setMessage("Creating event table with variant IDs");

            BufferedDataTable logWithVariant =
                    createLogWithVariantTableOptimized(
                            exec.createSubExecutionContext(0.20),
                            table,
                            traceToVariant,
                            settings
                    );

            exec.setProgress(1.0, "Finished");

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
            ExecutionContext exec,
            BufferedDataTable table,
            Map<Long, String> sequenceHashToVariant,
            TraceVariantVisNodeSettings settings)
            throws CanceledExecutionException {

        Map<String, String> result = new HashMap<>();

        DataTableSpec spec = table.getSpec();
        int traceIdx = spec.findColumnIndex(settings.t_classifier);
        int eventIdx = spec.findColumnIndex(settings.e_classifier);
        long totalRows = Math.max(table.size(), 1L);
        long processedRows = 0L;

        String currentTrace = null;
        long currentHash = 1L;

        for (DataRow row : table) {
            processedRows++;

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

            updateProgress(exec,
                    processedRows,
                    totalRows,
                    "Assigning variants to traces");
        }

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
            TraceVariantVisNodeSettings settings)
            throws CanceledExecutionException {

        BufferedDataContainer container =
                exec.createDataContainer(createVariantTableSpec(settings));

        var list = variants.getVariants();
        long totalVariants = Math.max(list.size(), 1L);

        for (int i = 0; i < list.size(); i++) {

            TraceVariant v = list.get(i);

            container.addRowToTable(new DefaultRow(
                    RowKey.createRowKey((long) i),
                    new StringCell("Variant_" + (i + 1)),
                    new IntCell(v.getFrequency()),
                    new StringCell(String.join(DELIMITER, v.getActivities()))
            ));

            exec.setProgress((i + 1) / (double) totalVariants,
                    "Writing variant summary rows");
            exec.checkCanceled();
        }

        container.close();
        return container.getTable();
    }

    private static BufferedDataTable createLogWithVariantTableOptimized(
            ExecutionContext exec,
            BufferedDataTable table,
            Map<String, String> traceToVariant,
            TraceVariantVisNodeSettings settings)
            throws CanceledExecutionException {

        DataTableSpec newSpec =
                createLogWithVariantSpec(table.getSpec(), settings);

        BufferedDataContainer container =
                exec.createDataContainer(newSpec);

        DataTableSpec spec = table.getSpec();
        int traceIdx = spec.findColumnIndex(settings.t_classifier);
        long totalRows = Math.max(table.size(), 1L);
        long processedRows = 0L;

        for (DataRow row : table) {
            processedRows++;

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

            updateProgress(exec,
                    processedRows,
                    totalRows,
                    "Writing rows with variant IDs");
        }

        container.close();
        return container.getTable();
    }

    private static void updateProgress(ExecutionContext exec,
                                       long processed,
                                       long total,
                                       String message)
            throws CanceledExecutionException {

        if (processed == total || processed % PROGRESS_UPDATE_INTERVAL == 0) {
            exec.setProgress(processed / (double) total,
                    message + " (" + processed + "/" + total + ")");
            exec.checkCanceled();
        }
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
