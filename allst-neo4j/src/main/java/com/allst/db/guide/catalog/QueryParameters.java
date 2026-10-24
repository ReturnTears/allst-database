package com.allst.db.guide.catalog;

import com.allst.db.guide.support.LuceneQuerySanitizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class QueryParameters {

    private QueryParameters() {
    }

    public static Map<String, Object> merge(NamedReadQuery query, Map<String, String> raw) {
        Map<String, Object> params = new LinkedHashMap<>(query.getDefaultParams());
        if (raw == null) {
            return params;
        }
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            if (!params.containsKey(entry.getKey()) || entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                continue;
            }
            params.put(entry.getKey(), coerce(params.get(entry.getKey()), entry.getValue().trim(), entry.getKey()));
        }
        return params;
    }

    public static void sanitizeFulltext(NamedReadQuery query, Map<String, Object> params) {
        String name = query.getFulltextParam();
        if (name == null || !params.containsKey(name) || params.get(name) == null) {
            return;
        }
        params.put(name, LuceneQuerySanitizer.escape(String.valueOf(params.get(name))));
    }

    private static Object coerce(Object sample, String raw, String name) {
        try {
            if (sample instanceof Integer) {
                return Integer.valueOf(raw);
            }
            if (sample instanceof Long) {
                return Long.valueOf(raw);
            }
            if (sample instanceof Double) {
                return Double.valueOf(raw);
            }
            if (sample instanceof Boolean) {
                if (!raw.equals("true") && !raw.equals("false")) {
                    throw new IllegalArgumentException("参数 " + name + " 只能是 true 或 false");
                }
                return Boolean.valueOf(raw);
            }
            return raw;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("参数 " + name + " 不是数字");
        }
    }

    public static Set<String> categories() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList("syntax", "function", "business", "tuning")));
    }
}
