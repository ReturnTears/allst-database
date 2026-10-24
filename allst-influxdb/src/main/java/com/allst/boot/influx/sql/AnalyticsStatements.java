package com.allst.boot.influx.sql;

import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.schema.InfluxSchema;

import java.util.List;
import java.util.Locale;

public final class AnalyticsStatements {

    public static final List<String> AGGREGATES = List.of(
            "mean", "max", "min", "sum", "count", "stddev", "spread", "median", "first", "last", "percentile");
    public static final List<String> TRANSFORMS = List.of(
            "derivative", "non_negative_derivative", "difference", "moving_average", "cumulative_sum", "elapsed");
    public static final List<String> EXPRESSIONS = List.of(
            "celsius_to_fahrenheit", "milli_scale", "abs_mean");
    private static final List<String> FILLS = List.of("none", "null", "linear", "previous", "0");

    private AnalyticsStatements() {
    }

    public static String aggregate(String retentionPolicy, String function, Integer percentile,
                                   String timePredicate, String deviceId, String region, String metricType,
                                   String window, String fill, int limit) {
        String fn = normalize(function, AGGREGATES, "聚合函数");
        String expression;
        if ("percentile".equals(fn)) {
            if (percentile == null || percentile < 0 || percentile > 100) {
                throw new IllegalArgumentException("percentile 必须在 0 到 100 之间");
            }
            expression = "percentile(" + value() + ", " + percentile + ")";
        } else {
            expression = fn + "(" + value() + ")";
        }
        return select(retentionPolicy, expression, timePredicate, deviceId, region, metricType, window, fill, limit);
    }

    public static String transform(String retentionPolicy, String function, Integer points, String unit,
                                   String timePredicate, String deviceId, String region, String metricType,
                                   String window, String fill, int limit) {
        String fn = normalize(function, TRANSFORMS, "变换函数");
        String safeWindow = InfluxQl.requireWindow(window);
        String unitText = unit == null || unit.isBlank() ? safeWindow : InfluxQl.requireWindow(unit);
        String expression;
        switch (fn) {
            case "derivative":
            case "non_negative_derivative":
            case "elapsed":
                expression = fn + "(mean(" + value() + "), " + unitText + ")";
                break;
            case "difference":
            case "cumulative_sum":
                expression = fn + "(mean(" + value() + "))";
                break;
            case "moving_average":
                int size = points == null ? 5 : points;
                if (size < 1 || size > 100) {
                    throw new IllegalArgumentException("moving_average 的点数必须在 1 到 100 之间");
                }
                expression = "moving_average(mean(" + value() + "), " + size + ")";
                break;
            default:
                throw new IllegalArgumentException("不支持的变换函数");
        }
        return select(retentionPolicy, expression, timePredicate, deviceId, region, metricType, safeWindow, fill, limit);
    }

    public static String calculate(String retentionPolicy, String expression,
                                   String timePredicate, String deviceId, String region, String metricType,
                                   String window, String fill, int limit) {
        String name = normalize(expression, EXPRESSIONS, "运算");
        String computed;
        switch (name) {
            case "celsius_to_fahrenheit":
                computed = "mean(" + value() + ") * 9 / 5 + 32";
                break;
            case "milli_scale":
                computed = "mean(" + value() + ") * 1000";
                break;
            case "abs_mean":
                computed = "abs(mean(" + value() + "))";
                break;
            default:
                throw new IllegalArgumentException("不支持的运算");
        }
        return select(retentionPolicy, computed, timePredicate, deviceId, region, metricType, window, fill, limit);
    }

    private static String select(String retentionPolicy, String expression, String timePredicate,
                                 String deviceId, String region, String metricType,
                                 String window, String fill, int limit) {
        if (metricType == null) {
            throw new IllegalArgumentException("函数计算必须指定 metricType，避免把不同物理量算到一起");
        }
        int safeLimit = limit < 1 || limit > InfluxLimits.ANALYTICS_LIMIT_MAX
                ? -1
                : limit;
        if (safeLimit < 0) {
            throw new IllegalArgumentException("limit 必须在 1 到 " + InfluxLimits.ANALYTICS_LIMIT_MAX + " 之间");
        }
        String safeWindow = InfluxQl.requireWindow(window);
        String safeFill = fill(fill);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(expression).append(" AS ").append(value())
                .append(" FROM ")
                .append(InfluxQl.quoteIdent(retentionPolicy)).append('.')
                .append(InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW))
                .append(" WHERE ").append(timePredicate);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_DEVICE_ID, deviceId);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_REGION, region);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_METRIC_TYPE, metricType);
        sql.append(" GROUP BY time(").append(safeWindow).append("), ")
                .append(InfluxQl.quoteIdent(InfluxSchema.TAG_DEVICE_ID)).append(", ")
                .append(InfluxQl.quoteIdent(InfluxSchema.TAG_REGION)).append(", ")
                .append(InfluxQl.quoteIdent(InfluxSchema.TAG_METRIC_TYPE))
                .append(" FILL(").append(safeFill).append(")")
                .append(" LIMIT ").append(safeLimit)
                .append(" SLIMIT ").append(InfluxLimits.SERIES_LIMIT);
        return sql.toString();
    }

    private static String value() {
        return InfluxQl.quoteIdent(InfluxSchema.FIELD_VALUE);
    }

    private static String fill(String fill) {
        String value = fill == null || fill.isBlank() ? "none" : fill.trim().toLowerCase(Locale.ROOT);
        if (!FILLS.contains(value)) {
            throw new IllegalArgumentException("fill 只允许 " + String.join("、", FILLS));
        }
        return value;
    }

    private static String normalize(String name, List<String> allowed, String label) {
        if (name == null) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        String value = name.trim().toLowerCase(Locale.ROOT);
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException(label + "只允许 " + String.join("、", allowed));
        }
        return value;
    }
}
