package org.pm4knime.node.visualizations.logviews.tracevariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.node.DefaultView;

final class TraceVariantView {

    static DefaultView.DefaultInitialData<Map<String, Object>>
    createInitialData(DefaultView.RequireInitialData r) {

    	return r.data(vi -> {

            BufferedDataTable summaryTable =
                    vi.getInternalTables()[0];

            Map<String, Object> root = new HashMap<>();
            Map<String, Object> variantData = new HashMap<>();

            int numberOfTraces = 0;

            var variantsList = new ArrayList<Map<String, Object>>();
            Set<String> allActivities = new LinkedHashSet<>();

			for (var row : summaryTable) {

                String variantId = row.getCell(0).toString();
                int frequency = ((IntCell) row.getCell(1)).getIntValue();
                String activitySequence = row.getCell(2).toString();

                List<String> activities =
                    Arrays.asList(activitySequence.split(" → "));

                Map<String, Object> v = new HashMap<>();
                v.put("variantId", variantId);
                v.put("frequency", frequency);
                v.put("activities", activities);  

                variantsList.add(v);

                allActivities.addAll(activities);
            }
            variantData.put("numberOfTraces", numberOfTraces);
            variantData.put("variants", variantsList);
            variantData.put("activities",
            	    new ArrayList<>(new HashSet<>(allActivities)));

            root.put("variants", variantData);

            return root;
        });
    }
}