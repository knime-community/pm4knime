package org.pm4knime.node.visualizations.logviews.tracevariant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.node.DefaultView;

final class TraceVariantView {

    private static final String DELIMITER = " \u2192 ";

    static DefaultView.DefaultInitialData<Map<String, Object>>
    createInitialData(DefaultView.RequireInitialData r) {

        return r.data(vi -> {

            BufferedDataTable summaryTable =
                    vi.getInternalTables()[0];

            Map<String, Object> root = new HashMap<>(2);
            Map<String, Object> variantData = new HashMap<>(3);

            List<Map<String, Object>> variantsList =
                    new ArrayList<>((int)summaryTable.size());

            Set<String> allActivities = new LinkedHashSet<>();

            int numberOfTraces = 0;

            for (var row : summaryTable) {

                String variantId = row.getCell(0).toString();
                int frequency = ((IntCell)row.getCell(1)).getIntValue();
                String activitySequence = row.getCell(2).toString();

                numberOfTraces += frequency;

                List<String> activities =
                        splitFast(activitySequence);

                Map<String, Object> v = new HashMap<>(3);
                v.put("variantId", variantId);
                v.put("frequency", frequency);
                v.put("activities", activities);

                variantsList.add(v);
                allActivities.addAll(activities);
            }

            variantData.put("numberOfTraces", numberOfTraces);
            variantData.put("variants", variantsList);
            variantData.put("activities",
                    new ArrayList<>(allActivities));

            root.put("variants", variantData);

            return root;
        });
    }

    private static List<String> splitFast(String input) {

        List<String> result = new ArrayList<>();

        int start = 0;
        int delimLength = DELIMITER.length();
        int index;

        while ((index = input.indexOf(DELIMITER, start)) >= 0) {
            result.add(input.substring(start, index));
            start = index + delimLength;
        }

        result.add(input.substring(start));

        return result;
    }
}
