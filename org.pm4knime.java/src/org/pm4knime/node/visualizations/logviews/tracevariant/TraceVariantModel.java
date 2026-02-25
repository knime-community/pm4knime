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

    // ---------------------------------------------------------
    // CONFIGURE
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // EXECUTE
    // ---------------------------------------------------------
    static void execute(DefaultModel.ExecuteInput i,
                        DefaultModel.ExecuteOutput o) {
    	
    	try {

    		TraceVariantVisNodeSettings settings =
        	        (TraceVariantVisNodeSettings) i.getParameters();

        	BufferedDataTable table =
        	        (BufferedDataTable) i.getInPortObject(0);

            // Sort by case ID + timestamp
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

            // Build variant representation
            TraceVariantRepresentation variants =
        	    new TraceVariantRepresentation(
        	        table,
        	        settings.t_classifier,
        	        settings.e_classifier
        	    );

            var mapping = buildTraceToVariantMapping(table, variants, settings);

            var summary = createVariantSummaryTable(
                i.getExecutionContext(),
                variants,
                settings
            );

            var logWithVariant = createLogWithVariantTable(
                i.getExecutionContext(),
                table,
                mapping,
                settings
            );

            o.setOutData(0, summary);
            o.setOutData(1, logWithVariant);

            // Important: pass data to view
            o.setInternalData(summary);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // ---------------------------------------------------------
    // HELPER METHODS
    // ---------------------------------------------------------

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

        List<DataColumnSpec> cols = new ArrayList<>();

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
                new StringCell(String.join(" → ", v.getActivities()))
            ));
        }

        container.close();
        return container.getTable();
    }

    private static BufferedDataTable createLogWithVariantTable(
        ExecutionContext exec,
        BufferedDataTable table,
        Map<String, String> traceToVariant,
        TraceVariantVisNodeSettings settings) {

        DataTableSpec newSpec =
            createLogWithVariantSpec(table.getSpec(), settings);

        BufferedDataContainer container =
            exec.createDataContainer(newSpec);

        int traceIdx =
            table.getSpec().findColumnIndex(settings.t_classifier);

        for (DataRow row : table) {

            List<DataCell> cells = new ArrayList<>();

            for (DataCell cell : row) {
                cells.add(cell);
            }

            String variantId =
                traceToVariant.getOrDefault(
                    row.getCell(traceIdx).toString(),
                    "UNKNOWN"
                );

            cells.add(new StringCell(variantId));

            container.addRowToTable(
                new DefaultRow(row.getKey(), cells)
            );
        }

        container.close();
        return container.getTable();
    }

    private static Map<String, String> buildTraceToVariantMapping(
        BufferedDataTable table,
        TraceVariantRepresentation variants,
        TraceVariantVisNodeSettings settings) {

        Map<String, String> sequenceToVariant =
            new HashMap<>();

        var list = variants.getVariants();

        for (int i = 0; i < list.size(); i++) {
            String seq =
                String.join(" → ", list.get(i).getActivities());
            sequenceToVariant.put(seq, "Variant_" + (i + 1));
        }

        int traceIdx =
            table.getSpec().findColumnIndex(settings.t_classifier);

        int eventIdx =
            table.getSpec().findColumnIndex(settings.e_classifier);

        Map<String, List<String>> traceActivities =
            new LinkedHashMap<>();

        for (DataRow row : table) {
            traceActivities
                .computeIfAbsent(
                    row.getCell(traceIdx).toString(),
                    k -> new ArrayList<>())
                .add(row.getCell(eventIdx).toString());
        }

        Map<String, String> result = new HashMap<>();

        traceActivities.forEach((traceId, acts) -> {
            String seq = String.join(" → ", acts);
            result.put(traceId,
                sequenceToVariant.getOrDefault(seq, "UNKNOWN"));
        });

        return result;
    }
}