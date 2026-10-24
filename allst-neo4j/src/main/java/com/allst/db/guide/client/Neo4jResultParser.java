package com.allst.db.guide.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Neo4jResultParser {

    private Neo4jResultParser() {
    }

    public static List<CypherRowSet> parse(JsonNode root, long elapsedMs) {
        if (root == null || root.isNull()) {
            throw new Neo4jException(502, "EmptyResponse", "Neo4j 返回空响应");
        }
        JsonNode errors = root.get("errors");
        if (errors != null && errors.isArray() && errors.size() > 0) {
            throw Neo4jException.fromCypher(text(errors.get(0), "code"), joinErrors(errors));
        }
        List<CypherRowSet> rowSets = new ArrayList<>();
        JsonNode results = root.get("results");
        if (results != null && results.isArray()) {
            for (JsonNode result : results) {
                rowSets.add(toRowSet(result, elapsedMs));
            }
        }
        return rowSets;
    }

    private static CypherRowSet toRowSet(JsonNode result, long elapsedMs) {
        List<String> columns = new ArrayList<>();
        JsonNode columnNode = result.get("columns");
        if (columnNode != null && columnNode.isArray()) {
            for (JsonNode column : columnNode) {
                columns.add(column.asText());
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        JsonNode data = result.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode item : data) {
                JsonNode row = item.get("row");
                Map<String, Object> mapped = new LinkedHashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    Object value = row != null && row.size() > i ? toJava(row.get(i)) : null;
                    mapped.put(columns.get(i), value);
                }
                rows.add(mapped);
            }
        }
        return new CypherRowSet(columns, rows, toMap(result.get("stats")), toMap(result.get("plan")), elapsedMs);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(JsonNode node) {
        if (node == null || node.isNull() || !node.isObject()) {
            return null;
        }
        return (Map<String, Object>) toJava(node);
    }

    private static String joinErrors(JsonNode errors) {
        StringBuilder message = new StringBuilder();
        for (JsonNode error : errors) {
            if (message.length() > 0) {
                message.append("; ");
            }
            message.append(text(error, "message"));
        }
        return message.toString();
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return "";
        }
        return node.get(field).asText();
    }

    static Object toJava(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isIntegralNumber()) {
            long value = node.longValue();
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }
            return value;
        }
        if (node.isFloatingPointNumber()) {
            return node.doubleValue();
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            node.forEach(item -> list.add(toJava(item)));
            return list;
        }
        if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> map.put(entry.getKey(), toJava(entry.getValue())));
            return map;
        }
        return node.asText();
    }
}
