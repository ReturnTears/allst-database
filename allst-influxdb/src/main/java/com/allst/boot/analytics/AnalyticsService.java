package com.allst.boot.analytics;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.common.InfluxResults;
import com.allst.boot.influx.common.TimeRange;
import com.allst.boot.influx.common.TimeRanges;
import com.allst.boot.influx.config.InfluxProperties;
import com.allst.boot.influx.sql.AnalyticsStatements;
import com.allst.boot.metric.MetricRules;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AnalyticsService {

    private final InfluxGateway gateway;
    private final InfluxProperties properties;

    public AnalyticsService(InfluxGateway gateway, InfluxProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public Map<String, Object> aggregate(String function, Integer percentile, String deviceId, String region,
                                         String metricType, String range, String start, String end,
                                         String window, String fill, Integer limit) {
        String sql = AnalyticsStatements.aggregate(properties.getRetentionPolicy(), function, percentile,
                time(range, start, end), tag(deviceId), tagOptional(region), requiredType(metricType),
                window(window), fill, limit(limit));
        return execute(sql);
    }

    public Map<String, Object> transform(String function, Integer points, String unit, String deviceId, String region,
                                         String metricType, String range, String start, String end,
                                         String window, String fill, Integer limit) {
        String safeWindow = window(window);
        String sql = AnalyticsStatements.transform(properties.getRetentionPolicy(), function, points, unit,
                time(range, start, end), tag(deviceId), tagOptional(region), requiredType(metricType),
                safeWindow, fill, limit(limit));
        return execute(sql);
    }

    public Map<String, Object> calculate(String expression, String deviceId, String region, String metricType,
                                         String range, String start, String end, String window, String fill,
                                         Integer limit) {
        String sql = AnalyticsStatements.calculate(properties.getRetentionPolicy(), expression,
                time(range, start, end), tag(deviceId), tagOptional(region), requiredType(metricType),
                window(window), fill, limit(limit));
        return execute(sql);
    }

    private Map<String, Object> execute(String sql) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("influxql", sql);
        body.put("rows", InfluxResults.rows(gateway.command(sql)));
        return body;
    }

    private static String time(String range, String start, String end) {
        TimeRange timeRange = TimeRanges.parse(range, start, end, InfluxLimits.ANALYTICS_MAX_SPAN);
        return timeRange.predicate();
    }

    private static String requiredType(String metricType) {
        return MetricRules.requiredTag(metricType, "metricType");
    }

    private static String tag(String deviceId) {
        return MetricRules.optionalTag(deviceId, "deviceId");
    }

    private static String tagOptional(String region) {
        return MetricRules.optionalTag(region, "region");
    }

    private static String window(String window) {
        return window == null || window.isBlank() ? "1m" : window;
    }

    private static int limit(Integer limit) {
        return limit == null ? InfluxLimits.ANALYTICS_LIMIT_DEFAULT : limit;
    }
}
