package com.allst.boot.metric;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.config.InfluxProperties;
import org.influxdb.dto.Point;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricWriteService {

    static final String[][] SEED_SERIES = {
            {"dev-001", "cn-east", "temperature"},
            {"dev-001", "cn-east", "humidity"},
            {"dev-002", "cn-north", "temperature"},
            {"dev-002", "cn-north", "humidity"},
            {"dev-003", "cn-south", "voltage"}
    };

    private final InfluxGateway gateway;
    private final InfluxProperties properties;

    public MetricWriteService(InfluxGateway gateway, InfluxProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public Map<String, Object> write(MetricWriteCommand command) {
        return writeSync(List.of(MetricRules.validate(command)));
    }

    public Map<String, Object> writeBatch(MetricBatchCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        return writeSync(MetricRules.validateBatch(command.getPoints()));
    }

    public Map<String, Object> writeAsync(MetricWriteCommand command) {
        ValidMetric metric = MetricRules.validate(command);
        Point point = DevicePoints.toPoint(metric);
        gateway.writeAsync(point);
        Map<String, Object> body = base("async", 1);
        body.put("line", DevicePoints.line(point));
        body.put("flushIntervalMs", properties.getBatch().getFlushIntervalMs());
        body.put("hint", "点先进入客户端缓冲。调用 /api/influx/metrics/flush，或等待刷盘间隔后再查询");
        return body;
    }

    public Map<String, Object> seed() {
        Instant end = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant start = end.minus(Duration.ofMinutes(60));
        List<ValidMetric> metrics = new ArrayList<>();
        for (int minute = 0; minute < 60; minute++) {
            Instant time = start.plus(Duration.ofMinutes(minute));
            for (String[] series : SEED_SERIES) {
                metrics.add(new ValidMetric(series[0], series[1], series[2],
                        seedValue(series[2], minute), seedStatus(series[2], minute), time));
            }
        }
        Map<String, Object> body = writeSync(metrics);
        body.put("from", start.toString());
        body.put("to", end.toString());
        body.put("series", SEED_SERIES.length);
        body.put("idempotent", "同一分钟内重复调用会覆盖相同时间戳的点");
        return body;
    }

    public Map<String, Object> flush() {
        gateway.flush();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("flushed", true);
        return body;
    }

    private Map<String, Object> writeSync(List<ValidMetric> metrics) {
        List<Point> points = new ArrayList<>(metrics.size());
        for (ValidMetric metric : metrics) {
            points.add(DevicePoints.toPoint(metric));
        }
        gateway.writeSync(points);
        Map<String, Object> body = base("sync", metrics.size());
        if (points.size() == 1) {
            body.put("line", DevicePoints.line(points.get(0)));
        }
        return body;
    }

    private Map<String, Object> base(String mode, int accepted) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accepted", accepted);
        body.put("mode", mode);
        body.put("database", properties.getDatabase());
        body.put("retentionPolicy", properties.getRetentionPolicy());
        body.put("measurement", "device_metric");
        return body;
    }

    public static double seedValue(String metricType, int minute) {
        double wave = Math.sin(minute / 8.0);
        double raw;
        switch (metricType) {
            case "temperature":
                raw = 26 + wave * 4;
                break;
            case "humidity":
                raw = 55 + wave * 10;
                break;
            case "voltage":
                raw = 220 + wave * 2;
                break;
            default:
                raw = 1 + wave;
                break;
        }
        return Math.round(raw * 10.0) / 10.0;
    }

    private static long seedStatus(String metricType, int minute) {
        if ("temperature".equals(metricType) && seedValue(metricType, minute) < 23) {
            return 0L;
        }
        return 1L;
    }
}
