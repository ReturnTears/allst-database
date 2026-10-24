package com.allst.boot.mutation;

import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.reading.ReadingRules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MutationRules {

    private MutationRules() {
    }

    public static String sku(String value) {
        return Identifiers.requireId(value, "skuId");
    }

    public static String warehouse(String value) {
        return Identifiers.requireLabel(value, "warehouse");
    }

    public static int quantity(Integer value, String label) {
        if (value == null || value < 0 || value > Limits.STOCK_MAX) {
            throw new IllegalArgumentException(label + "必须在 0 到 " + Limits.STOCK_MAX + " 之间");
        }
        return value;
    }

    public static long delta(Integer value) {
        if (value == null || value < 1 || value > Limits.COUNTER_DELTA_MAX) {
            throw new IllegalArgumentException("delta 必须在 1 到 " + Limits.COUNTER_DELTA_MAX + " 之间。计数器重试会重复累加，这个接口不提供减量。");
        }
        return value;
    }

    public static String firmware(String value) {
        return Identifiers.optionalName(value, "firmware");
    }

    public static Set<String> labels(List<String> values, boolean required) {
        if (values == null || values.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("labels 不能为空");
            }
            return Set.of();
        }
        if (values.size() > Limits.COLLECTION_MAX) {
            throw new IllegalArgumentException("labels 最多 " + Limits.COLLECTION_MAX + " 个");
        }
        Set<String> copy = new LinkedHashSet<>();
        for (String value : values) {
            if (!copy.add(Identifiers.requireLabel(value, "label"))) {
                throw new IllegalArgumentException("labels 里有重复值");
            }
        }
        return Set.copyOf(copy);
    }

    public static List<String> channels(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        if (values.size() > Limits.COLLECTION_MAX) {
            throw new IllegalArgumentException("channels 最多 " + Limits.COLLECTION_MAX + " 个");
        }
        List<String> copy = new ArrayList<>(values.size());
        for (String value : values) {
            copy.add(Identifiers.requireLabel(value, "channel"));
        }
        return List.copyOf(copy);
    }

    public static Map<String, Double> thresholds(Map<String, Double> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        if (values.size() > Limits.TAGS_MAX) {
            throw new IllegalArgumentException("thresholds 最多 " + Limits.TAGS_MAX + " 个");
        }
        Map<String, Double> copy = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            String metric = ReadingRules.requireMetric(key);
            if (value == null || !Double.isFinite(value) || value < -1_000d || value > 100_000d) {
                throw new IllegalArgumentException("threshold 必须是 -1000 到 100000 之间的有限数值");
            }
            copy.put(metric, value);
        });
        return Map.copyOf(copy);
    }

    public static double threshold(Double value) {
        if (value == null || !Double.isFinite(value) || value < -1_000d || value > 100_000d) {
            throw new IllegalArgumentException("threshold 必须是 -1000 到 100000 之间的有限数值");
        }
        return value;
    }
}
