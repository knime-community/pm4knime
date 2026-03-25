package org.pm4knime.node.visualizations.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.pm4knime.portobject.AbstractJSONPortObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class GraphSvgRenderer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAPS = new TypeReference<>() {
    };

    private static final double MARGIN = 36;
    private static final double RANK_SPACING = 180;
    private static final double LANE_SPACING = 120;

    private GraphSvgRenderer() {
    }

    public static String render(final AbstractJSONPortObject portObject) {
        final Map<String, Object> normalized = JsonViewData.normalize(portObject);
        final List<NodeData> nodes = toNodeData(normalized.get("nodes"));
        final List<EdgeData> edges = toEdgeData(normalized.get("links"));

        if (nodes.isEmpty()) {
            return emptySvg("No graph data available.");
        }

        final boolean topToBottom = isTopToBottom(nodes);
        final boolean processTree = isProcessTree(nodes);
        final Layout layout = layout(nodes, edges, topToBottom);

        final StringBuilder svg = new StringBuilder(16_384);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
            .append(format(layout.width))
            .append("\" height=\"")
            .append(format(layout.height))
            .append("\" viewBox=\"0 0 ")
            .append(format(layout.width))
            .append(' ')
            .append(format(layout.height))
            .append("\" fill=\"none\">");

        svg.append("<defs>")
            .append("<marker id=\"arrow-blue\" markerWidth=\"10\" markerHeight=\"10\" refX=\"8\" refY=\"3\" orient=\"auto\">")
            .append("<path d=\"M0,0 L0,6 L8,3 z\" fill=\"#334155\"/></marker>")
            .append("<marker id=\"arrow-red\" markerWidth=\"10\" markerHeight=\"10\" refX=\"8\" refY=\"3\" orient=\"auto\">")
            .append("<path d=\"M0,0 L0,6 L8,3 z\" fill=\"#dc2626\"/></marker>")
            .append("<marker id=\"arrow-orange\" markerWidth=\"10\" markerHeight=\"10\" refX=\"8\" refY=\"3\" orient=\"auto\">")
            .append("<path d=\"M0,0 L0,6 L8,3 z\" fill=\"#d97706\"/></marker>")
            .append("</defs>");

        svg.append("<rect x=\"0\" y=\"0\" width=\"")
            .append(format(layout.width))
            .append("\" height=\"")
            .append(format(layout.height))
            .append("\" fill=\"#f8fafc\"/>");

        final Map<String, NodeBox> nodeBoxes = new HashMap<>();
        for (NodeData node : nodes) {
            final PositionedNode positioned = layout.positions.get(node.id);
            nodeBoxes.put(node.id, new NodeBox(positioned.x, positioned.y, positioned.width, positioned.height));
        }

        for (EdgeData edge : edges) {
            final NodeBox source = nodeBoxes.get(edge.source);
            final NodeBox target = nodeBoxes.get(edge.target);
            if (source == null || target == null) {
                continue;
            }

            final double x1 = topToBottom ? source.centerX() : source.right();
            final double y1 = topToBottom ? source.bottom() : source.centerY();
            final double x2 = topToBottom ? target.centerX() : target.left();
            final double y2 = topToBottom ? target.top() : target.centerY();

            final EdgeStyle style = edgeStyle(edge, processTree);
            svg.append("<line x1=\"")
                .append(format(x1))
                .append("\" y1=\"")
                .append(format(y1))
                .append("\" x2=\"")
                .append(format(x2))
                .append("\" y2=\"")
                .append(format(y2))
                .append("\" stroke=\"")
                .append(style.color)
                .append("\" stroke-width=\"2.5\"");

            if (style.dashed) {
                svg.append(" stroke-dasharray=\"6 4\"");
            }
            if (style.markerId != null) {
                svg.append(" marker-end=\"url(#").append(style.markerId).append(")\"");
            }
            svg.append("/>");

            final String label = edgeLabel(edge, processTree);
            if (!label.isBlank()) {
                final double labelX = (x1 + x2) / 2.0;
                final double labelY = (y1 + y2) / 2.0 - 8.0;
                svg.append("<rect x=\"")
                    .append(format(labelX - 16))
                    .append("\" y=\"")
                    .append(format(labelY - 14))
                    .append("\" width=\"32\" height=\"18\" rx=\"6\" fill=\"#ffffff\" fill-opacity=\"0.88\"/>")
                    .append("<text x=\"")
                    .append(format(labelX))
                    .append("\" y=\"")
                    .append(format(labelY))
                    .append("\" text-anchor=\"middle\" font-size=\"11\" font-family=\"Arial, sans-serif\" fill=\"#0f172a\">")
                    .append(escapeXml(label))
                    .append("</text>");
            }
        }

        for (NodeData node : nodes) {
            renderNode(svg, node, layout.positions.get(node.id));
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private static void renderNode(final StringBuilder svg, final NodeData node, final PositionedNode positioned) {
        final NodeStyle style = nodeStyle(node);
        final double x = positioned.x;
        final double y = positioned.y;
        final double width = positioned.width;
        final double height = positioned.height;

        if (node.isPlace()) {
            final double cx = x + width / 2.0;
            final double cy = y + height / 2.0;
            final double r = Math.min(width, height) / 2.0 - 2.0;

            svg.append("<circle cx=\"")
                .append(format(cx))
                .append("\" cy=\"")
                .append(format(cy))
                .append("\" r=\"")
                .append(format(r))
                .append("\" fill=\"#ffffff\" stroke=\"#475569\" stroke-width=\"2.5\"/>");

            if (node.finalMarking) {
                svg.append("<circle cx=\"")
                    .append(format(cx))
                    .append("\" cy=\"")
                    .append(format(cy))
                    .append("\" r=\"")
                    .append(format(r - 5))
                    .append("\" fill=\"none\" stroke=\"#475569\" stroke-width=\"2\"/>");
            }
            if (node.initialMarking) {
                svg.append("<circle cx=\"")
                    .append(format(cx))
                    .append("\" cy=\"")
                    .append(format(cy))
                    .append("\" r=\"6\" fill=\"#15803d\"/>");
            }
            return;
        }

        svg.append("<rect x=\"")
            .append(format(x))
            .append("\" y=\"")
            .append(format(y))
            .append("\" width=\"")
            .append(format(width))
            .append("\" height=\"")
            .append(format(height))
            .append("\" rx=\"")
            .append(node.isTransition() ? "4" : "12")
            .append("\" fill=\"")
            .append(style.fill)
            .append("\" stroke=\"")
            .append(style.stroke)
            .append("\" stroke-width=\"2.5\"/>");

        final String label = displayLabel(node);
        if (!label.isBlank()) {
            svg.append("<text x=\"")
                .append(format(x + width / 2.0))
                .append("\" y=\"")
                .append(format(y + height / 2.0 + 5))
                .append("\" text-anchor=\"middle\" font-size=\"13\" font-family=\"Arial, sans-serif\" fill=\"#0f172a\">")
                .append(escapeXml(label))
                .append("</text>");
        }
    }

    private static Layout layout(final List<NodeData> nodes, final List<EdgeData> edges, final boolean topToBottom) {
        final Map<String, Set<String>> outgoing = new LinkedHashMap<>();
        final Map<String, Integer> indegree = new HashMap<>();
        for (NodeData node : nodes) {
            outgoing.put(node.id, new TreeSet<>());
            indegree.put(node.id, 0);
        }

        for (EdgeData edge : edges) {
            if (!outgoing.containsKey(edge.source) || !indegree.containsKey(edge.target)) {
                continue;
            }
            outgoing.get(edge.source).add(edge.target);
            indegree.put(edge.target, indegree.get(edge.target) + 1);
        }

        final Map<String, Integer> ranks = new HashMap<>();
        final ArrayDeque<String> queue = new ArrayDeque<>();
        nodes.stream()
            .map(n -> n.id)
            .filter(id -> indegree.getOrDefault(id, 0) == 0)
            .sorted()
            .forEach(id -> {
                ranks.put(id, 0);
                queue.add(id);
            });

        if (queue.isEmpty()) {
            final String first = nodes.stream().map(n -> n.id).min(String::compareTo).orElse(nodes.get(0).id);
            ranks.put(first, 0);
            queue.add(first);
        }

        final Map<String, Integer> indegreeCopy = new HashMap<>(indegree);
        final Set<String> processed = new TreeSet<>();

        while (!queue.isEmpty()) {
            final String source = queue.removeFirst();
            processed.add(source);
            final int sourceRank = ranks.getOrDefault(source, 0);

            for (String target : outgoing.getOrDefault(source, Set.of())) {
                ranks.put(target, Math.max(ranks.getOrDefault(target, 0), sourceRank + 1));
                indegreeCopy.put(target, indegreeCopy.get(target) - 1);
                if (indegreeCopy.get(target) == 0) {
                    queue.add(target);
                }
            }
        }

        int fallbackRank = ranks.values().stream().max(Integer::compareTo).orElse(0) + 1;
        for (NodeData node : nodes) {
            if (!processed.contains(node.id) && !ranks.containsKey(node.id)) {
                ranks.put(node.id, fallbackRank++);
            }
        }

        final Map<Integer, List<NodeData>> byRank = new HashMap<>();
        for (NodeData node : nodes) {
            byRank.computeIfAbsent(ranks.getOrDefault(node.id, 0), ignored -> new ArrayList<>()).add(node);
        }
        byRank.values().forEach(list -> list.sort(Comparator
            .comparing((NodeData n) -> n.type == null ? "" : n.type)
            .thenComparing(n -> n.label == null ? "" : n.label)
            .thenComparing(n -> n.id)));

        final Map<String, PositionedNode> positions = new HashMap<>();
        double maxX = 0;
        double maxY = 0;

        for (Map.Entry<Integer, List<NodeData>> entry : byRank.entrySet()) {
            final int rank = entry.getKey().intValue();
            final List<NodeData> rankNodes = entry.getValue();

            for (int lane = 0; lane < rankNodes.size(); lane++) {
                final NodeData node = rankNodes.get(lane);
                final double width = estimateWidth(node);
                final double height = estimateHeight(node);
                final double primary = MARGIN + rank * RANK_SPACING;
                final double secondary = MARGIN + lane * LANE_SPACING;

                final double x = topToBottom ? secondary : primary;
                final double y = topToBottom ? primary : secondary;

                positions.put(node.id, new PositionedNode(x, y, width, height));
                maxX = Math.max(maxX, x + width);
                maxY = Math.max(maxY, y + height);
            }
        }

        return new Layout(maxX + MARGIN, maxY + MARGIN, positions);
    }

    private static boolean isTopToBottom(final Collection<NodeData> nodes) {
        return nodes.stream().map(node -> node.type)
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .anyMatch(type -> type.equals("activity") || type.equals("artificial start") || type.equals("artificial end"));
    }

    private static boolean isProcessTree(final Collection<NodeData> nodes) {
        return nodes.stream().map(node -> node.type)
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .anyMatch(type -> type.equals("operator") || type.equals("manual") || type.equals("automatic"));
    }

    private static List<NodeData> toNodeData(final Object value) {
        final List<Map<String, Object>> rows = OBJECT_MAPPER.convertValue(value, LIST_OF_MAPS);
        final List<NodeData> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(new NodeData(
                asString(row.get("id")),
                asString(row.get("type")),
                asString(row.get("label")),
                asBoolean(row.get("initial")) || asBoolean(row.get("i_marking")),
                asBoolean(row.get("final")) || asBoolean(row.get("f_marking"))));
        }
        return result;
    }

    private static List<EdgeData> toEdgeData(final Object value) {
        final List<Map<String, Object>> rows = OBJECT_MAPPER.convertValue(value, LIST_OF_MAPS);
        final List<EdgeData> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(new EdgeData(
                asString(row.get("source")),
                asString(row.get("target")),
                asString(row.get("type")),
                row.get("frequency")));
        }
        return result;
    }

    private static NodeStyle nodeStyle(final NodeData node) {
        final String type = lower(node.type);
        if ("artificial start".equals(type)) {
            return new NodeStyle("#c8fcc0", "#167f06");
        }
        if ("artificial end".equals(type)) {
            return new NodeStyle("#fecaca", "#b91c1c");
        }
        if ("activity".equals(type)) {
            return new NodeStyle("#e2e8f0", "#64748b");
        }
        if ("operator".equals(type)) {
            return new NodeStyle("#dbeafe", "#60a5fa");
        }
        if ("transition".equals(type)) {
            return new NodeStyle("#dbeafe", "#2563eb");
        }
        return new NodeStyle("#f8fafc", "#64748b");
    }

    private static EdgeStyle edgeStyle(final EdgeData edge, final boolean processTree) {
        final String type = lower(edge.type);
        if (type.contains("uncertainedge") || type.contains("hybriddirecteduncertaingraphedge")) {
            return new EdgeStyle("#dc2626", true, processTree ? null : "arrow-red");
        }
        if (type.contains("longdepedge") || type.contains("hybriddirectedlongdepgraphedge")) {
            return new EdgeStyle("#d97706", false, processTree ? null : "arrow-orange");
        }
        return new EdgeStyle("#334155", false, processTree ? null : "arrow-blue");
    }

    private static String edgeLabel(final EdgeData edge, final boolean processTree) {
        if (edge.frequency == null) {
            return "";
        }
        if (edge.frequency instanceof Number number) {
            final int value = number.intValue();
            if (processTree) {
                if (value == -1) {
                    return "do";
                }
                if (value <= 0) {
                    return "";
                }
            }
            return Integer.toString(value);
        }
        return edge.frequency.toString();
    }

    private static String displayLabel(final NodeData node) {
        final String type = lower(node.type);
        if ("operator".equals(type)) {
            return switch (lower(node.label)) {
                case "xlp" -> "loop";
                case "xor" -> "xor";
                case "and" -> "and";
                case "seq" -> "seq";
                default -> node.label;
            };
        }
        return node.label == null ? "" : node.label;
    }

    private static double estimateWidth(final NodeData node) {
        if (node.isPlace()) {
            return 42;
        }
        final int labelLength = Math.max(displayLabel(node).length(), 1);
        return Math.max(70, 18 + labelLength * 8);
    }

    private static double estimateHeight(final NodeData node) {
        return node.isPlace() ? 42 : 40;
    }

    private static String emptySvg(final String message) {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"640\" height=\"80\" viewBox=\"0 0 640 80\">"
            + "<rect width=\"640\" height=\"80\" fill=\"#f8fafc\"/>"
            + "<text x=\"24\" y=\"44\" font-size=\"16\" font-family=\"Arial, sans-serif\" fill=\"#0f172a\">"
            + escapeXml(message)
            + "</text></svg>";
    }

    private static String asString(final Object value) {
        return value == null ? "" : value.toString();
    }

    private static boolean asBoolean(final Object value) {
        if (value instanceof Boolean bool) {
            return bool.booleanValue();
        }
        return Boolean.parseBoolean(asString(value));
    }

    private static String lower(final String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static String format(final double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String escapeXml(final String value) {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private record NodeData(String id, String type, String label, boolean initialMarking, boolean finalMarking) {
        boolean isPlace() {
            return "place".equalsIgnoreCase(type);
        }

        boolean isTransition() {
            return "transition".equalsIgnoreCase(type);
        }
    }

    private record EdgeData(String source, String target, String type, Object frequency) {
    }

    private record NodeStyle(String fill, String stroke) {
    }

    private record EdgeStyle(String color, boolean dashed, String markerId) {
    }

    private record Layout(double width, double height, Map<String, PositionedNode> positions) {
    }

    private record PositionedNode(double x, double y, double width, double height) {
    }

    private record NodeBox(double x, double y, double width, double height) {
        double left() {
            return x;
        }

        double right() {
            return x + width;
        }

        double top() {
            return y;
        }

        double bottom() {
            return y + height;
        }

        double centerX() {
            return x + width / 2.0;
        }

        double centerY() {
            return y + height / 2.0;
        }
    }
}
