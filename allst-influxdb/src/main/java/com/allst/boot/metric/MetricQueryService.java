package com.allst.boot.metric;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.common.InfluxResults;
import com.allst.boot.influx.common.TimeRange;
import com.allst.boot.influx.common.TimeRanges;
import com.allst.boot.influx.config.InfluxProperties;
import com.allst.boot.influx.sql.InfluxQl;
import com.allst.boot.influx.sql.MetricStatements;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MetricQueryService {

    private final InfluxGateway gateway;
    private final InfluxProperties properties;

    public MetricQueryService(InfluxGateway gateway, InfluxProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public Map<String, Object> query(String deviceId, String region, String metricType,
                                     String range, String start, String end,
                                     String cursor, String order, Integer limit) {
        String safeDevice = MetricRules.requiredTag(deviceId, "deviceId");
        String safeType = MetricRules.requiredTag(metricType, "metricType");
        String safeRegion = MetricRules.optionalTag(region, "region");
        boolean ascending = parseOrder(order);
        int safeLimit = limit == null ? InfluxLimits.RAW_LIMIT_DEFAULT : limit;
        TimeRange timeRange = TimeRanges.parse(range, start, end, InfluxLimits.RAW_MAX_SPAN);
        String cursorText = cursorPredicate(cursor, ascending, timeRange);
        String sql = MetricStatements.selectRaw(properties.getRetentionPolicy(), timeRange.predicate(),
                cursorText, safeDevice, safeRegion, safeType, ascending, safeLimit, InfluxLimits.RAW_LIMIT_MAX);
        List<Map<String, Object>> rows = InfluxResults.rows(gateway.command(sql));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("influxql", sql);
        body.put("rows", rows);
        body.put("seriesCount", seriesCount(rows));
        body.put("nextCursor", nextCursor(rows, safeLimit));
        if (seriesCount(rows) > 1) {
            body.put("hint", "结果里有多条 series，nextCursor 不会返回。补上 region 后再翻页");
        }
        return body;
    }

    public Map<String, Object> latest(String deviceId, String region, String metricType, String range) {
        String safeDevice = MetricRules.requiredTag(deviceId, "deviceId");
        String safeRegion = MetricRules.optionalTag(region, "region");
        String safeType = MetricRules.optionalTag(metricType, "metricType");
        TimeRange timeRange = TimeRanges.parse(range == null || range.isBlank() ? "7d" : range,
                null, null, InfluxLimits.RAW_MAX_SPAN);
        String sql = MetricStatements.selectLatest(properties.getRetentionPolicy(), timeRange.predicate(),
                safeDevice, safeRegion, safeType);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("influxql", sql);
        body.put("rows", InfluxResults.rows(gateway.command(sql)));
        return body;
    }

    public Map<String, Object> delete(String deviceId, String region, String metricType,
                                      String range, String start, String end) {
        String safeDevice = MetricRules.optionalTag(deviceId, "deviceId");
        String safeRegion = MetricRules.optionalTag(region, "region");
        String safeType = MetricRules.optionalTag(metricType, "metricType");
        TimeRange timeRange = TimeRanges.parse(range, start, end, InfluxLimits.RAW_MAX_SPAN);
        String sql = MetricStatements.delete(properties.getRetentionPolicy(), timeRange.predicate(),
                safeDevice, safeRegion, safeType);
        gateway.command(sql);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deleted", true);
        body.put("influxql", sql);
        return body;
    }

    public List<Map<String, Object>> exportRows(String deviceId, String region, String metricType,
                                                String range, String start, String end, int limit) {
        String safeDevice = MetricRules.optionalTag(deviceId, "deviceId");
        String safeRegion = MetricRules.optionalTag(region, "region");
        String safeType = MetricRules.optionalTag(metricType, "metricType");
        if (safeDevice == null && safeType == null) {
            throw new IllegalArgumentException("导出必须指定 deviceId 或 metricType，避免一次扫完整条 measurement");
        }
        TimeRange timeRange = TimeRanges.parse(range, start, end, InfluxLimits.RAW_MAX_SPAN);
        String sql = MetricStatements.selectRaw(properties.getRetentionPolicy(), timeRange.predicate(),
                null, safeDevice, safeRegion, safeType, true, limit, InfluxLimits.EXPORT_MAX);
        return InfluxResults.rows(gateway.command(sql));
    }

    private static boolean parseOrder(String order) {
        String value = order == null || order.isBlank() ? "desc" : order.trim().toLowerCase();
        if ("asc".equals(value)) {
            return true;
        }
        if ("desc".equals(value)) {
            return false;
        }
        throw new IllegalArgumentException("order 只允许 asc 或 desc");
    }

    private static String cursorPredicate(String cursor, boolean ascending, TimeRange timeRange) {
        String text = InfluxQl.blankToNull(cursor);
        if (text == null) {
            return null;
        }
        Instant instant = TimeRanges.parseInstant(text);
        if (instant.isBefore(timeRange.getStart()) || !instant.isBefore(timeRange.getEnd())) {
            throw new IllegalArgumentException("cursor 必须落在本次查询的时间范围内");
        }
        return MetricStatements.cursorPredicate(ascending, InfluxQl.format(instant));
    }

    private static int seriesCount(List<Map<String, Object>> rows) {
        Set<String> series = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            series.add(String.valueOf(row.get("device_id")) + "|"
                    + row.get("region") + "|" + row.get("metric_type"));
        }
        return series.size();
    }

    private static String nextCursor(List<Map<String, Object>> rows, int limit) {
        if (seriesCount(rows) != 1 || rows.size() < limit) {
            return null;
        }
        Object time = rows.get(rows.size() - 1).get("time");
        return time == null ? null : String.valueOf(time);
    }
}
