package com.allst.db.guide.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把 Neo4j HTTP 返回的 plan 树摊平成算子列表，便于对照 EXPLAIN / PROFILE。
 */
public final class PlanFormatter {

    private PlanFormatter() {
    }

    public static List<Map<String, Object>> operators(Map<String, Object> plan) {
        if (plan == null || plan.isEmpty()) {
            return Collections.emptyList();
        }
        Object root = plan.get("root");
        List<Map<String, Object>> operators = new ArrayList<>();
        walk(root instanceof Map ? root : plan, operators);
        return operators;
    }

    public static String planText(Map<String, Object> plan) {
        if (plan == null) {
            return null;
        }
        Object root = plan.get("root") instanceof Map ? plan.get("root") : plan;
        if (!(root instanceof Map)) {
            return null;
        }
        Map<?, ?> rootMap = (Map<?, ?>) root;
        Object text = first(rootMap.get("string-representation"), rootMap.get("stringRepresentation"));
        return text == null ? null : String.valueOf(text);
    }

    /**
     * EXPLAIN 没有实际命中次数时返回 null。PROFILE 返回各算子 DbHits 之和。
     */
    public static Long totalDbHits(List<Map<String, Object>> operators) {
        boolean seen = false;
        long total = 0L;
        for (Map<String, Object> operator : operators) {
            Object hits = operator.get("dbHits");
            if (hits instanceof Number) {
                seen = true;
                total += ((Number) hits).longValue();
            }
        }
        return seen ? total : null;
    }

    @SuppressWarnings("unchecked")
    private static void walk(Object node, List<Map<String, Object>> sink) {
        if (!(node instanceof Map)) {
            return;
        }
        Map<String, Object> operator = (Map<String, Object>) node;
        Map<String, Object> args = operator.get("args") instanceof Map
                ? (Map<String, Object>) operator.get("args")
                : Collections.emptyMap();
        if (operator.get("operatorType") != null) {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("operatorType", operator.get("operatorType"));
            view.put("details", first(operator.get("Details"), args.get("Details")));
            view.put("estimatedRows", first(operator.get("EstimatedRows"), args.get("EstimatedRows")));
            view.put("dbHits", first(operator.get("DbHits"), args.get("DbHits")));
            view.put("rows", first(operator.get("Rows"), args.get("Rows")));
            view.put("identifiers", operator.get("identifiers"));
            sink.add(view);
        }
        Object children = operator.get("children");
        if (children instanceof List) {
            for (Object child : (List<Object>) children) {
                walk(child, sink);
            }
        }
    }

    private static Object first(Object preferred, Object fallback) {
        return preferred != null ? preferred : fallback;
    }
}
