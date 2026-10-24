package com.allst.boot.reading;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Instants;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReadingQueryService {

    private final CqlGateway gateway;
    private final CassandraProperties properties;
    private final ZoneId zone;

    public ReadingQueryService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> query(String deviceId, String dayText, String metricType, String fromText, String toText,
                                      String order, Integer limit) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.optionalMetric(metricType);
        int rowLimit = Limits.limit(limit, Limits.QUERY_DEFAULT, Limits.QUERY_MAX, "limit");
        Instant from = blank(fromText) ? null : Instants.parse(fromText, zone);
        Instant to = blank(toText) ? null : Instants.parse(toText, zone);
        if ((from != null || to != null) && metric == null) {
            throw new IllegalArgumentException("按时间范围查询时必须带 metricType，聚簇列不能跳过");
        }
        if (ReadingRules.ascending(order) && metric == null) {
            throw new IllegalArgumentException("指定 order 时必须带 metricType");
        }
        DayBuckets.requireInside(day, from, to, zone);
        String cql = metric == null
                ? Statements.selectPartition(properties.getKeyspace())
                : Statements.selectReadings(properties.getKeyspace(), from != null, to != null, ReadingRules.ascending(order));
        List<Map<String, Object>> rows = Rows.list(gateway.query(cql, builder -> {
            builder.setString("device_id", device).setLocalDate("day_bucket", day).setInt("row_limit", rowLimit);
            if (metric != null) {
                builder.setString("metric_type", metric);
            }
            bindRange(builder, from, to);
        }), rowLimit);
        return result(properties.getKeyspace() + ".sensor_reading", rows, rowLimit);
    }

    public Map<String, Object> latest(String deviceId, String dayText, String metricType) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.requireMetric(metricType);
        Map<String, Object> row = Rows.first(gateway.query(Statements.selectLatest(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day).setString("metric_type", metric)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("row", row);
        body.put("hint", "聚簇列 recorded_at 是倒序，LIMIT 1 就是该指标最新的一行");
        return body;
    }

    public Map<String, Object> latestPerMetric(String deviceId, String dayText) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.selectLatestPerMetric(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day)), 20);
        Map<String, Object> body = result(properties.getKeyspace() + ".sensor_reading", rows, 20);
        body.put("hint", "GROUP BY metric_type 返回每个指标的第一行。表是时间倒序，所以这一行是最新值。");
        return body;
    }

    public Map<String, Object> byRegion(String region, String dayText, String metricType, Integer limit) {
        String regionId = Identifiers.requireRegion(region);
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.optionalMetric(metricType);
        int rowLimit = Limits.limit(limit, Limits.QUERY_DEFAULT, Limits.QUERY_MAX, "limit");
        List<Map<String, Object>> rows = Rows.list(gateway.query(
                Statements.selectByRegion(properties.getKeyspace(), metric != null), builder -> {
                    builder.setString("region", regionId).setLocalDate("day_bucket", day).setInt("row_limit", rowLimit);
                    if (metric != null) {
                        builder.setString("metric_type", metric);
                    }
                }), rowLimit);
        Map<String, Object> body = result(properties.getKeyspace() + ".reading_by_region", rows, rowLimit);
        body.put("hint", "这张表由写入接口双写。原表没有 region 分区键，不能靠 ALLOW FILTERING 代替。");
        return body;
    }

    public List<Map<String, Object>> exportRows(String deviceId, String dayText, String metricType, int limit) {
        Map<String, Object> queried = query(deviceId, dayText, metricType, null, null, "desc", limit);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) queried.get("rows");
        return rows;
    }

    private static void bindRange(BoundStatementBuilder builder, Instant from, Instant to) {
        if (from != null) {
            builder.setInstant("range_from", from);
        }
        if (to != null) {
            builder.setInstant("range_to", to);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static Map<String, Object> result(String table, List<Map<String, Object>> rows, int limit) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", table);
        body.put("rows", rows);
        body.put("count", rows.size());
        body.put("limit", limit);
        body.put("truncated", rows.size() == limit);
        return body;
    }
}
