package com.allst.boot.influx.sql;

import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.schema.InfluxSchema;

public final class MetricStatements {

    private MetricStatements() {
    }

    public static String selectRaw(String retentionPolicy, String timePredicate, String cursorPredicate,
                                   String deviceId, String region, String metricType,
                                   boolean ascending, int limit, int maxLimit) {
        int safeLimit = limit(limit, maxLimit);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(InfluxQl.quoteIdent(InfluxSchema.FIELD_VALUE)).append(", ")
                .append(InfluxQl.quoteIdent(InfluxSchema.FIELD_STATUS))
                .append(" FROM ")
                .append(InfluxQl.quoteIdent(retentionPolicy)).append('.')
                .append(InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW))
                .append(" WHERE ").append(timePredicate);
        if (cursorPredicate != null) {
            sql.append(" AND ").append(cursorPredicate);
        }
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_DEVICE_ID, deviceId);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_REGION, region);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_METRIC_TYPE, metricType);
        sql.append(" ORDER BY time ").append(ascending ? "ASC" : "DESC");
        sql.append(" LIMIT ").append(safeLimit);
        return sql.toString();
    }

    public static String selectLatest(String retentionPolicy, String timePredicate,
                                      String deviceId, String region, String metricType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT last(").append(InfluxQl.quoteIdent(InfluxSchema.FIELD_VALUE)).append(") AS ")
                .append(InfluxQl.quoteIdent(InfluxSchema.FIELD_VALUE))
                .append(", last(").append(InfluxQl.quoteIdent(InfluxSchema.FIELD_STATUS)).append(") AS ")
                .append(InfluxQl.quoteIdent(InfluxSchema.FIELD_STATUS))
                .append(" FROM ")
                .append(InfluxQl.quoteIdent(retentionPolicy)).append('.')
                .append(InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW))
                .append(" WHERE ").append(timePredicate);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_DEVICE_ID, deviceId);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_REGION, region);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_METRIC_TYPE, metricType);
        sql.append(" GROUP BY ")
                .append(InfluxQl.quoteIdent(InfluxSchema.TAG_DEVICE_ID)).append(", ")
                .append(InfluxQl.quoteIdent(InfluxSchema.TAG_REGION)).append(", ")
                .append(InfluxQl.quoteIdent(InfluxSchema.TAG_METRIC_TYPE));
        return sql.toString();
    }

    public static String delete(String retentionPolicy, String timePredicate,
                                String deviceId, String region, String metricType) {
        if (deviceId == null && region == null && metricType == null) {
            throw new IllegalArgumentException("删除必须至少指定 deviceId、region、metricType 中的一个");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ")
                .append(InfluxQl.quoteIdent(retentionPolicy)).append('.')
                .append(InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW))
                .append(" WHERE ").append(timePredicate);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_DEVICE_ID, deviceId);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_REGION, region);
        InfluxQl.appendEquals(sql, InfluxSchema.TAG_METRIC_TYPE, metricType);
        return sql.toString();
    }

    public static String cursorPredicate(boolean ascending, String timestamp) {
        return "time " + (ascending ? ">" : "<") + " " + InfluxQl.quoteTimestamp(timestamp);
    }

    private static int limit(int limit, int max) {
        if (limit < 1 || limit > max) {
            throw new IllegalArgumentException("limit 必须在 1 到 " + max + " 之间");
        }
        return limit;
    }
}
