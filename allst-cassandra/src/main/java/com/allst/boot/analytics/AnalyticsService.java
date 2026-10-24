package com.allst.boot.analytics;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Instants;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.allst.boot.reading.DayBuckets;
import com.allst.boot.reading.ReadingIds;
import com.allst.boot.reading.ReadingRules;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalyticsService {

    private final CqlGateway gateway;
    private final CassandraProperties properties;
    private final ZoneId zone;

    public AnalyticsService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> aggregate(String deviceId, String dayText, String metricType, String function) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.requireMetric(metricType);
        String cql = Statements.aggregate(properties.getKeyspace(), function);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fn", function.trim().toLowerCase());
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("row", Rows.first(gateway.query(cql, builder -> builder.setString("device_id", device)
                .setLocalDate("day_bucket", day).setString("metric_type", metric))));
        body.put("hint", "聚合带了完整分区键和 metric_type，只扫一个分区里的一个指标。不要对整表做 count。");
        return body;
    }

    public Map<String, Object> group(String deviceId, String dayText) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.groupByMetric(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day)), 20);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("rows", rows);
        body.put("count", rows.size());
        body.put("hint", "GROUP BY 只能按主键前缀。这里可以按 metric_type，不能按小时，小时聚合在连续查询的降采样里做。");
        return body;
    }

    public Map<String, Object> scalar(String deviceId, String dayText, String metricType, String time, String readingId) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.requireMetric(metricType);
        Instant recordedAt = Instants.parse(time, zone);
        DayBuckets.requireInside(day, recordedAt, null, zone);
        UUID id = ReadingIds.parse(readingId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("row", Rows.first(gateway.query(Statements.scalar(properties.getKeyspace()), builder -> builder
                .setString("device_id", device)
                .setLocalDate("day_bucket", day)
                .setString("metric_type", metric)
                .setInstant("recorded_at", recordedAt)
                .setUuid("reading_id", id))));
        body.put("hint", "writetime 的单位是微秒。ttl 为空表示这一格没有单独的 TTL，表级 7 天仍然生效。");
        return body;
    }

    public Map<String, Object> arithmetic(String deviceId, String dayText, String metricType, Integer limit) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.requireMetric(metricType);
        int rowLimit = Limits.limit(limit, 20, Limits.QUERY_MAX, "limit");
        List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.arithmetic(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day)
                        .setString("metric_type", metric).setInt("row_limit", rowLimit)), rowLimit);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("expression", "sensor_value * 1.8 + 32");
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("rows", rows);
        body.put("count", rows.size());
        body.put("hint", "运算写死在服务端。不要把调用方传来的表达式拼进 CQL。");
        return body;
    }

    public Map<String, Object> token(String deviceId, String dayText) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        Map<String, Object> row = Rows.first(gateway.query(Statements.partitionToken(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("found", row != null);
        body.put("row", row);
        body.put("hint", "token 由分区键算出来。分区里还没有行时这里读不到，先写入再查。");
        return body;
    }

    public Map<String, Object> serverTime() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("row", Rows.first(gateway.query(Statements.serverTime(), builder -> {
        })));
        body.put("hint", "now() 在协调节点上生成，重试会得到新的 timeuuid，不能拿来做需要幂等的主键。");
        return body;
    }
}
