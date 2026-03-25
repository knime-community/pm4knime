package org.pm4knime.node.visualizations.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.node.DefaultView;
import org.pm4knime.portobject.AbstractJSONPortObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonViewData {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private JsonViewData() {
    }

    public static DefaultView.DefaultInitialData<Map<String, Object>>
        graphInitialData(final DefaultView.RequireInitialData r) {

        return r.data(vi -> {
            final var portObject = (AbstractJSONPortObject)vi.getInternalPortObjects()[0];
            final Map<String, Object> data = new HashMap<>();
            data.put("kind", "graph");
            data.put("graph", normalize(portObject));
            return data;
        });
    }

    public static DefaultView.DefaultInitialData<Map<String, Object>>
        bpmnInitialData(final DefaultView.RequireInitialData r) {

        return r.data(vi -> {
            final var portObject = (AbstractJSONPortObject)vi.getInternalPortObjects()[0];
            final var normalized = normalize(portObject);

            final Map<String, Object> data = new HashMap<>();
            data.put("kind", "bpmn");
            data.put("xml", firstString(normalized.get("xml")));
            data.put("layouter", firstBoolean(normalized.get("layouter")));
            return data;
        });
    }

    public static Map<String, Object> normalize(final AbstractJSONPortObject portObject) {
        return OBJECT_MAPPER.convertValue(portObject.getJSON(), MAP_TYPE);
    }

    private static String firstString(final Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            final Object first = list.get(0);
            return first == null ? "" : first.toString();
        }
        return value == null ? "" : value.toString();
    }

    private static boolean firstBoolean(final Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            final Object first = list.get(0);
            if (first instanceof Boolean bool) {
                return bool.booleanValue();
            }
            return Boolean.parseBoolean(String.valueOf(first));
        }
        if (value instanceof Boolean bool) {
            return bool.booleanValue();
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
