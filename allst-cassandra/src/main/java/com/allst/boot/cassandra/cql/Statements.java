package com.allst.boot.cassandra.cql;

import com.allst.boot.cassandra.common.Identifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Statements {

    public static final String READING_COLUMNS = "device_id, day_bucket, metric_type, recorded_at, reading_id, "
            + "region, sensor_value, status, tags, device_model";

    private Statements() {
    }

    public static List<String> all(String keyspace, String strategy, int factor, Map<String, Integer> datacenters) {
        String ks = Identifiers.requireKeyspace(keyspace);
        List<String> statements = new ArrayList<>();
        statements.add(createKeyspace(ks, strategy, factor, datacenters));
        statements.add(sensorReading(ks));
        statements.add(readingByRegion(ks));
        statements.add(sensorHourly(ks));
        statements.add(deviceHitCounter(ks));
        statements.add(skuStock(ks));
        statements.add(deviceProfile(ks));
        statements.add(statusIndex(ks));
        statements.add(readingByMetricView(ks));
        return List.copyOf(statements);
    }

    public static String createKeyspace(String keyspace, String strategy, int factor, Map<String, Integer> datacenters) {
        return "CREATE KEYSPACE IF NOT EXISTS " + Identifiers.requireKeyspace(keyspace)
                + " WITH replication = " + replicationMap(strategy, factor, datacenters)
                + " AND durable_writes = true";
    }

    public static String alterKeyspace(String keyspace, String strategy, int factor, Map<String, Integer> datacenters) {
        return "ALTER KEYSPACE " + Identifiers.requireKeyspace(keyspace)
                + " WITH replication = " + replicationMap(strategy, factor, datacenters)
                + " AND durable_writes = true";
    }

    public static String replicationMap(String strategy, int factor, Map<String, Integer> datacenters) {
        if (strategy == null) {
            throw new IllegalArgumentException("replication strategy 不能为空");
        }
        String name = strategy.trim();
        if ("SimpleStrategy".equals(name)) {
            Identifiers.requireFactor(factor);
            return "{'class': 'SimpleStrategy', 'replication_factor': " + factor + "}";
        }
        if ("NetworkTopologyStrategy".equals(name)) {
            Identifiers.requireDatacenterFactors(datacenters);
            StringBuilder map = new StringBuilder("{'class': 'NetworkTopologyStrategy'");
            datacenters.forEach((dc, rf) -> map.append(", '").append(dc).append("': ").append(rf));
            map.append('}');
            return map.toString();
        }
        throw new IllegalArgumentException("replication strategy 只允许 SimpleStrategy 或 NetworkTopologyStrategy");
    }

    public static String sensorReading(String keyspace) {
        return """
                CREATE TABLE IF NOT EXISTS %s.sensor_reading (
                  device_id text,
                  day_bucket date,
                  metric_type text,
                  recorded_at timestamp,
                  reading_id uuid,
                  region text,
                  sensor_value double,
                  status int,
                  tags map<text, text>,
                  device_model text static,
                  PRIMARY KEY ((device_id, day_bucket), metric_type, recorded_at, reading_id)
                ) WITH CLUSTERING ORDER BY (metric_type ASC, recorded_at DESC, reading_id ASC)
                  AND default_time_to_live = 604800
                  AND gc_grace_seconds = 86400
                  AND compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': '1'}
                  AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
                  AND comment = 'raw readings, partition by device and day'
                """.formatted(Identifiers.requireKeyspace(keyspace));
    }

    public static String readingByRegion(String keyspace) {
        return """
                CREATE TABLE IF NOT EXISTS %s.reading_by_region (
                  region text,
                  day_bucket date,
                  metric_type text,
                  recorded_at timestamp,
                  device_id text,
                  reading_id uuid,
                  sensor_value double,
                  status int,
                  PRIMARY KEY ((region, day_bucket), metric_type, recorded_at, device_id, reading_id)
                ) WITH CLUSTERING ORDER BY (metric_type ASC, recorded_at DESC, device_id ASC, reading_id ASC)
                  AND default_time_to_live = 604800
                  AND gc_grace_seconds = 86400
                  AND compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': '1'}
                  AND comment = 'dual write for region queries'
                """.formatted(Identifiers.requireKeyspace(keyspace));
    }

    public static String sensorHourly(String keyspace) {
        return """
                CREATE TABLE IF NOT EXISTS %s.sensor_hourly (
                  device_id text,
                  day_bucket date,
                  hour_bucket timestamp,
                  metric_type text,
                  sample_count int,
                  value_sum double,
                  value_min double,
                  value_max double,
                  PRIMARY KEY ((device_id, day_bucket), hour_bucket, metric_type)
                ) WITH CLUSTERING ORDER BY (hour_bucket DESC, metric_type ASC)
                  AND default_time_to_live = 7776000
                  AND gc_grace_seconds = 86400
                  AND compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': '1'}
                  AND comment = 'hourly rollup written by the application'
                """.formatted(Identifiers.requireKeyspace(keyspace));
    }

    public static String deviceHitCounter(String keyspace) {
        return """
                CREATE TABLE IF NOT EXISTS %s.device_hit_counter (
                  device_id text,
                  day_bucket date,
                  metric_type text,
                  hits counter,
                  PRIMARY KEY ((device_id, day_bucket), metric_type)
                ) WITH comment = 'counter table, no regular columns and no ttl'
                """.formatted(Identifiers.requireKeyspace(keyspace));
    }

    public static String skuStock(String keyspace) {
        return """
                CREATE TABLE IF NOT EXISTS %s.sku_stock (
                  sku_id text PRIMARY KEY,
                  warehouse text,
                  quantity int
                ) WITH compaction = {'class': 'LeveledCompactionStrategy'}
                  AND comment = 'compare and set inventory'
                """.formatted(Identifiers.requireKeyspace(keyspace));
    }

    public static String deviceProfile(String keyspace) {
        return """
                CREATE TABLE IF NOT EXISTS %s.device_profile (
                  device_id text PRIMARY KEY,
                  firmware text,
                  labels set<text>,
                  thresholds map<text, double>,
                  channels list<text>
                ) WITH compaction = {'class': 'LeveledCompactionStrategy'}
                  AND comment = 'non-frozen collections'
                """.formatted(Identifiers.requireKeyspace(keyspace));
    }

    public static String statusIndex(String keyspace) {
        return "CREATE INDEX IF NOT EXISTS sensor_reading_status_idx ON "
                + Identifiers.requireKeyspace(keyspace) + ".sensor_reading (status)";
    }

    public static String readingByMetricView(String keyspace) {
        String ks = Identifiers.requireKeyspace(keyspace);
        return """
                CREATE MATERIALIZED VIEW IF NOT EXISTS %s.reading_by_metric AS
                  SELECT device_id, day_bucket, metric_type, recorded_at, reading_id, region, sensor_value, status
                  FROM %s.sensor_reading
                  WHERE device_id IS NOT NULL
                    AND day_bucket IS NOT NULL
                    AND metric_type IS NOT NULL
                    AND recorded_at IS NOT NULL
                    AND reading_id IS NOT NULL
                  PRIMARY KEY ((metric_type, day_bucket), recorded_at, device_id, reading_id)
                  WITH CLUSTERING ORDER BY (recorded_at DESC, device_id ASC, reading_id ASC)
                """.formatted(ks, ks);
    }

    public static String insertReading(String keyspace, boolean ttl) {
        return "INSERT INTO " + table(keyspace, "sensor_reading")
                + " (device_id, day_bucket, metric_type, recorded_at, reading_id, region, sensor_value, status, tags) "
                + "VALUES (:device_id, :day_bucket, :metric_type, :recorded_at, :reading_id, :region, :sensor_value, :status, :tags)"
                + (ttl ? " USING TTL :ttl" : "");
    }

    public static String insertDeviceModel(String keyspace) {
        return "INSERT INTO " + table(keyspace, "sensor_reading")
                + " (device_id, day_bucket, device_model) VALUES (:device_id, :day_bucket, :device_model)";
    }

    public static String insertByRegion(String keyspace, boolean ttl) {
        return "INSERT INTO " + table(keyspace, "reading_by_region")
                + " (region, day_bucket, metric_type, recorded_at, device_id, reading_id, sensor_value, status) "
                + "VALUES (:region, :day_bucket, :metric_type, :recorded_at, :device_id, :reading_id, :sensor_value, :status)"
                + (ttl ? " USING TTL :ttl" : "");
    }

    public static String selectPartition(String keyspace) {
        return "SELECT " + READING_COLUMNS + " FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket LIMIT :row_limit";
    }

    public static String selectReadings(String keyspace, boolean from, boolean to, boolean ascending) {
        StringBuilder cql = new StringBuilder();
        cql.append("SELECT ").append(READING_COLUMNS).append(" FROM ").append(table(keyspace, "sensor_reading"));
        cql.append(" WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type");
        if (from) {
            cql.append(" AND recorded_at >= :range_from");
        }
        if (to) {
            cql.append(" AND recorded_at < :range_to");
        }
        if (ascending) {
            cql.append(" ORDER BY recorded_at ASC, reading_id DESC");
        }
        cql.append(" LIMIT :row_limit");
        return cql.toString();
    }

    public static String selectLatest(String keyspace) {
        return "SELECT " + READING_COLUMNS + " FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type LIMIT 1";
    }

    public static String selectLatestPerMetric(String keyspace) {
        return "SELECT device_id, day_bucket, metric_type, recorded_at, reading_id, region, sensor_value, status FROM "
                + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket GROUP BY metric_type";
    }

    public static String selectByRegion(String keyspace, boolean metric) {
        StringBuilder cql = new StringBuilder();
        cql.append("SELECT region, day_bucket, metric_type, recorded_at, device_id, reading_id, sensor_value, status FROM ");
        cql.append(table(keyspace, "reading_by_region"));
        cql.append(" WHERE region = :region AND day_bucket = :day_bucket");
        if (metric) {
            cql.append(" AND metric_type = :metric_type");
        }
        cql.append(" LIMIT :row_limit");
        return cql.toString();
    }

    public static String aggregate(String keyspace, String function) {
        String select = switch (normalize(function)) {
            case "count" -> "count(sensor_value) AS sample_count";
            case "min" -> "min(sensor_value) AS value_min";
            case "max" -> "max(sensor_value) AS value_max";
            case "avg" -> "avg(sensor_value) AS value_avg";
            case "sum" -> "sum(sensor_value) AS value_sum";
            default -> throw new IllegalArgumentException("fn 只允许 count、min、max、avg、sum");
        };
        return "SELECT " + select + " FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type";
    }

    public static String groupByMetric(String keyspace) {
        return "SELECT metric_type, count(sensor_value) AS sample_count, min(sensor_value) AS value_min, "
                + "max(sensor_value) AS value_max, avg(sensor_value) AS value_avg, sum(sensor_value) AS value_sum FROM "
                + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket GROUP BY metric_type";
    }

    public static String scalar(String keyspace) {
        return "SELECT recorded_at, reading_id, sensor_value, writetime(sensor_value) AS value_writetime, "
                + "ttl(sensor_value) AS value_ttl, toUnixTimestamp(recorded_at) AS recorded_unix_ms, "
                + "toDate(recorded_at) AS recorded_date, token(device_id, day_bucket) AS partition_token, "
                + "CAST(sensor_value AS text) AS value_text FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type "
                + "AND recorded_at = :recorded_at AND reading_id = :reading_id";
    }

    public static String arithmetic(String keyspace) {
        return "SELECT recorded_at, reading_id, sensor_value, sensor_value * 1.8 + 32 AS fahrenheit FROM "
                + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type "
                + "LIMIT :row_limit";
    }

    public static String serverTime() {
        return "SELECT now() AS server_time, toTimestamp(now()) AS server_timestamp, toDate(now()) AS server_date, "
                + "blobAsText(textAsBlob('cassandra')) AS blob_round_trip FROM system.local";
    }

    public static String partitionToken(String keyspace) {
        return "SELECT token(device_id, day_bucket) AS partition_token FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket LIMIT 1";
    }

    public static String pageByDevice(String keyspace, boolean metric) {
        StringBuilder cql = new StringBuilder();
        cql.append("SELECT ").append(READING_COLUMNS).append(" FROM ").append(table(keyspace, "sensor_reading"));
        cql.append(" WHERE device_id = :device_id AND day_bucket = :day_bucket");
        if (metric) {
            cql.append(" AND metric_type = :metric_type");
        }
        return cql.toString();
    }

    public static String scan(String keyspace) {
        return "SELECT " + READING_COLUMNS + " FROM " + table(keyspace, "sensor_reading");
    }

    public static String insertHourly(String keyspace) {
        return "INSERT INTO " + table(keyspace, "sensor_hourly")
                + " (device_id, day_bucket, hour_bucket, metric_type, sample_count, value_sum, value_min, value_max) "
                + "VALUES (:device_id, :day_bucket, :hour_bucket, :metric_type, :sample_count, :value_sum, :value_min, :value_max)";
    }

    public static String selectHourly(String keyspace) {
        return "SELECT device_id, day_bucket, hour_bucket, metric_type, sample_count, value_sum, value_min, value_max FROM "
                + table(keyspace, "sensor_hourly")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket LIMIT :row_limit";
    }

    public static String insertStock(String keyspace) {
        return "INSERT INTO " + table(keyspace, "sku_stock")
                + " (sku_id, warehouse, quantity) VALUES (:sku_id, :warehouse, :quantity) IF NOT EXISTS";
    }

    public static String casStock(String keyspace) {
        return "UPDATE " + table(keyspace, "sku_stock")
                + " SET quantity = :quantity WHERE sku_id = :sku_id IF quantity = :expected";
    }

    public static String selectStock(String keyspace) {
        return "SELECT sku_id, warehouse, quantity FROM " + table(keyspace, "sku_stock") + " WHERE sku_id = :sku_id";
    }

    public static String updateCounter(String keyspace) {
        return "UPDATE " + table(keyspace, "device_hit_counter")
                + " SET hits = hits + :delta WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type";
    }

    public static String selectCounter(String keyspace) {
        return "SELECT device_id, day_bucket, metric_type, hits FROM " + table(keyspace, "device_hit_counter")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket";
    }

    public static String insertProfile(String keyspace) {
        return "INSERT INTO " + table(keyspace, "device_profile")
                + " (device_id, firmware, labels, thresholds, channels) "
                + "VALUES (:device_id, :firmware, :labels, :thresholds, :channels)";
    }

    public static String appendLabels(String keyspace) {
        return "UPDATE " + table(keyspace, "device_profile")
                + " SET labels = labels + :labels WHERE device_id = :device_id";
    }

    public static String updateThreshold(String keyspace) {
        return "UPDATE " + table(keyspace, "device_profile")
                + " SET thresholds[:metric_type] = :threshold WHERE device_id = :device_id";
    }

    public static String selectProfile(String keyspace) {
        return "SELECT device_id, firmware, labels, thresholds, channels FROM " + table(keyspace, "device_profile")
                + " WHERE device_id = :device_id";
    }

    public static String selectByStatus(String keyspace) {
        return "SELECT " + READING_COLUMNS + " FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket AND status = :status LIMIT :row_limit";
    }

    public static String selectByMetricView(String keyspace) {
        return "SELECT device_id, day_bucket, metric_type, recorded_at, reading_id, region, sensor_value, status FROM "
                + table(keyspace, "reading_by_metric")
                + " WHERE metric_type = :metric_type AND day_bucket = :day_bucket LIMIT :row_limit";
    }

    public static String selectTables() {
        return "SELECT table_name, comment FROM system_schema.tables WHERE keyspace_name = :keyspace_name";
    }

    public static String selectColumns() {
        return "SELECT table_name, column_name, kind, type, position FROM system_schema.columns "
                + "WHERE keyspace_name = :keyspace_name";
    }

    public static String selectIndexes() {
        return "SELECT index_name, table_name, kind, options FROM system_schema.indexes WHERE keyspace_name = :keyspace_name";
    }

    public static String selectViews() {
        return "SELECT view_name, base_table_name, where_clause FROM system_schema.views WHERE keyspace_name = :keyspace_name";
    }

    public static String selectLocal() {
        return "SELECT cluster_name, release_version, cql_version, native_protocol_version, data_center, rack, "
                + "host_id, partitioner FROM system.local";
    }

    public static String selectPeers() {
        return "SELECT peer, data_center, rack, release_version, host_id FROM system.peers";
    }

    public static String selectSettings() {
        return "SELECT name, value FROM system_views.settings";
    }

    public static String deleteRow(String keyspace) {
        return "DELETE FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type "
                + "AND recorded_at = :recorded_at AND reading_id = :reading_id";
    }

    public static String deleteRegionRow(String keyspace) {
        return "DELETE FROM " + table(keyspace, "reading_by_region")
                + " WHERE region = :region AND day_bucket = :day_bucket AND metric_type = :metric_type "
                + "AND recorded_at = :recorded_at AND device_id = :device_id AND reading_id = :reading_id";
    }

    public static String deleteRange(String keyspace) {
        return "DELETE FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket AND metric_type = :metric_type "
                + "AND recorded_at >= :range_from AND recorded_at < :range_to";
    }

    public static String deletePartition(String keyspace) {
        return "DELETE FROM " + table(keyspace, "sensor_reading")
                + " WHERE device_id = :device_id AND day_bucket = :day_bucket";
    }

    public static String deleteRegionPartition(String keyspace) {
        return "DELETE FROM " + table(keyspace, "reading_by_region")
                + " WHERE region = :region AND day_bucket = :day_bucket";
    }

    private static String table(String keyspace, String table) {
        return Identifiers.requireKeyspace(keyspace) + "." + table;
    }

    private static String normalize(String function) {
        return function == null ? "" : function.trim().toLowerCase(Locale.ROOT);
    }
}
