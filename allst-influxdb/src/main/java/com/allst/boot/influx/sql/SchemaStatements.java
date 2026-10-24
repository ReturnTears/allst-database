package com.allst.boot.influx.sql;

import com.allst.boot.influx.schema.InfluxSchema;

public final class SchemaStatements {

    private SchemaStatements() {
    }

    public static String createDatabase(String database) {
        return "CREATE DATABASE " + InfluxQl.quoteIdent(database);
    }

    public static String createRetention(String database, InfluxSchema.RetentionSpec spec, int replication) {
        return retention(database, spec, replication, false);
    }

    public static String alterRetention(String database, InfluxSchema.RetentionSpec spec, int replication) {
        return retention(database, spec, replication, true);
    }

    public static String dropRetention(String database, String name) {
        return "DROP RETENTION POLICY " + InfluxQl.quoteIdent(name) + " ON " + InfluxQl.quoteIdent(database);
    }

    public static String showRetentionPolicies(String database) {
        return "SHOW RETENTION POLICIES ON " + InfluxQl.quoteIdent(database);
    }

    public static String showContinuousQueries() {
        return "SHOW CONTINUOUS QUERIES";
    }

    public static String showFieldKeys() {
        return "SHOW FIELD KEYS FROM " + InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW);
    }

    public static String showSeriesCardinality() {
        return "SHOW SERIES CARDINALITY FROM " + InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW);
    }

    public static String showTagValues(String key, String timePredicate) {
        InfluxQl.requireIdent(key);
        if (!InfluxSchema.TAG_DEVICE_ID.equals(key)
                && !InfluxSchema.TAG_REGION.equals(key)
                && !InfluxSchema.TAG_METRIC_TYPE.equals(key)) {
            throw new IllegalArgumentException("tag key 只允许 device_id、region、metric_type");
        }
        return "SHOW TAG VALUES FROM " + InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW)
                + " WITH KEY = " + InfluxQl.quoteIdent(key)
                + " WHERE " + timePredicate;
    }

    public static String minuteContinuousQuery(String database) {
        return continuousQuery(database, InfluxSchema.CQ_1M, "1m", "5m",
                InfluxSchema.RP_90D, InfluxSchema.MEASUREMENT_1M);
    }

    public static String hourContinuousQuery(String database) {
        return continuousQuery(database, InfluxSchema.CQ_1H, "1h", "2h",
                InfluxSchema.RP_365D, InfluxSchema.MEASUREMENT_1H);
    }

    public static String dropContinuousQuery(String database, String name) {
        InfluxQl.requireIdent(name);
        if (!name.startsWith("cq_")) {
            throw new IllegalArgumentException("只允许删除 cq_ 前缀的连续查询");
        }
        return "DROP CONTINUOUS QUERY " + InfluxQl.quoteIdent(name) + " ON " + InfluxQl.quoteIdent(database);
    }

    public static String backfill(String targetRp, String targetMeasurement, String window, String timePredicate) {
        String safeWindow = InfluxQl.requireWindow(window);
        String value = InfluxQl.quoteIdent(InfluxSchema.FIELD_VALUE);
        return "SELECT mean(" + value + ") AS " + value
                + ", max(" + value + ") AS " + InfluxQl.quoteIdent(InfluxSchema.FIELD_MAX)
                + ", min(" + value + ") AS " + InfluxQl.quoteIdent(InfluxSchema.FIELD_MIN)
                + ", count(" + value + ") AS " + InfluxQl.quoteIdent(InfluxSchema.FIELD_COUNT)
                + " INTO " + InfluxQl.quoteIdent(targetRp) + "." + InfluxQl.quoteIdent(targetMeasurement)
                + " FROM " + InfluxQl.quoteIdent(InfluxSchema.RP_RAW) + "." + InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW)
                + " WHERE " + timePredicate
                + " GROUP BY time(" + safeWindow + "), *";
    }

    private static String retention(String database, InfluxSchema.RetentionSpec spec, int replication, boolean alter) {
        if (replication < 1 || replication > 3) {
            throw new IllegalArgumentException("replication 只允许 1 到 3");
        }
        InfluxQl.parseDuration(spec.getDuration());
        InfluxQl.parseDuration(spec.getShardDuration());
        String verb = alter ? "ALTER RETENTION POLICY " : "CREATE RETENTION POLICY ";
        String sql = verb + InfluxQl.quoteIdent(spec.getName())
                + " ON " + InfluxQl.quoteIdent(database)
                + " DURATION " + spec.getDuration()
                + " REPLICATION " + replication
                + " SHARD DURATION " + spec.getShardDuration();
        if (spec.isDefaultPolicy()) {
            sql += " DEFAULT";
        }
        return sql;
    }

    private static String continuousQuery(String database, String name, String every, String coverage,
                                          String targetRp, String targetMeasurement) {
        String value = InfluxQl.quoteIdent(InfluxSchema.FIELD_VALUE);
        return "CREATE CONTINUOUS QUERY " + InfluxQl.quoteIdent(name)
                + " ON " + InfluxQl.quoteIdent(database)
                + " RESAMPLE EVERY " + every + " FOR " + coverage
                + " BEGIN SELECT mean(" + value + ") AS " + value
                + ", max(" + value + ") AS " + InfluxQl.quoteIdent(InfluxSchema.FIELD_MAX)
                + ", min(" + value + ") AS " + InfluxQl.quoteIdent(InfluxSchema.FIELD_MIN)
                + ", count(" + value + ") AS " + InfluxQl.quoteIdent(InfluxSchema.FIELD_COUNT)
                + " INTO " + InfluxQl.quoteIdent(targetRp) + "." + InfluxQl.quoteIdent(targetMeasurement)
                + " FROM " + InfluxQl.quoteIdent(InfluxSchema.RP_RAW) + "." + InfluxQl.quoteIdent(InfluxSchema.MEASUREMENT_RAW)
                + " GROUP BY time(" + every + "), * END";
    }
}
