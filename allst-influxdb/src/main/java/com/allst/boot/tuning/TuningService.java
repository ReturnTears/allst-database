package com.allst.boot.tuning;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.InfluxAccessException;
import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.config.InfluxProperties;
import com.allst.boot.influx.schema.InfluxSchema;
import com.allst.boot.metric.DevicePoints;
import com.allst.boot.metric.ValidMetric;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TuningService {

    public static final String CLIENT_VERSION = "2.24";

    private final InfluxGateway gateway;
    private final InfluxProperties properties;

    public TuningService(InfluxGateway gateway, InfluxProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public Map<String, Object> profile() {
        InfluxProperties.Batch batch = properties.getBatch();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("client", "influxdb-java " + CLIENT_VERSION);
        body.put("url", properties.getUrl());
        body.put("database", properties.getDatabase());
        body.put("retentionPolicy", properties.getRetentionPolicy());
        body.put("gzip", properties.isGzip());
        body.put("consistency", properties.getConsistency());
        body.put("replicationFactor", properties.getReplicationFactor());
        body.put("batchEnabled", gateway.batchEnabled());
        body.put("actions", batch.getActions());
        body.put("flushIntervalMs", batch.getFlushIntervalMs());
        body.put("jitterIntervalMs", batch.getJitterIntervalMs());
        body.put("bufferLimit", batch.getBufferLimit());
        body.put("precision", batch.getPrecision());
        body.put("dropOnQueueExhaustion", batch.isDropOnQueueExhaustion());
        body.put("logLevel", properties.getLogLevel());
        try {
            Pong pong = gateway.ping();
            body.put("serverReachable", true);
            body.put("serverVersion", pong.getVersion());
            body.put("pingMs", pong.getResponseTime());
        } catch (InfluxAccessException ex) {
            body.put("serverReachable", false);
            body.put("serverError", ex.getMessage());
        }
        body.put("recommendations", recommendations());
        return body;
    }

    public Map<String, Object> benchmark(String mode, Integer points) {
        int count = points == null ? 1000 : points;
        if (count < 1 || count > InfluxLimits.BENCHMARK_MAX) {
            throw new IllegalArgumentException("points 必须在 1 到 " + InfluxLimits.BENCHMARK_MAX + " 之间");
        }
        String safeMode = mode == null || mode.isBlank() ? "sync" : mode.trim().toLowerCase(Locale.ROOT);
        List<Point> batch = new ArrayList<>(count);
        Instant now = Instant.now();
        for (int i = 0; i < count; i++) {
            ValidMetric metric = new ValidMetric("bench-001", "bench", "benchmark",
                    i % 100, 1L, now.minusMillis(count - (long) i));
            batch.add(DevicePoints.toPoint(metric));
        }
        long started = System.nanoTime();
        if ("sync".equals(safeMode)) {
            gateway.writeSync(batch);
        } else if ("async".equals(safeMode)) {
            for (Point point : batch) {
                gateway.writeAsync(point);
            }
            gateway.flush();
        } else {
            throw new IllegalArgumentException("mode 只允许 sync 或 async");
        }
        long elapsedMs = Math.max(1, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", safeMode);
        body.put("points", count);
        body.put("elapsedMs", elapsedMs);
        body.put("pointsPerSecond", count * 1000.0 / elapsedMs);
        body.put("deviceId", "bench-001");
        body.put("hint", "压测点落在 bench-001。用 DELETE /api/influx/metrics?deviceId=bench-001&metricType=benchmark&range=1h 清理");
        return body;
    }

    private List<String> recommendations() {
        List<String> notes = new ArrayList<>();
        InfluxProperties.Batch batch = properties.getBatch();
        notes.add("接口写入走同步 BatchPoints，调用返回时服务端已经确认。异步接口才进入客户端缓冲");
        notes.add("查询带时间范围，明细查询带 deviceId 和 metricType，函数计算不要混用不同 metricType");
        notes.add("追踪号、订单号、用户号不要做 tag。tag 基数上去之后，内存和查询都会变差");
        if (!properties.isGzip()) {
            notes.add("打开 influx.gzip，写入 body 会小一截");
        }
        if (!"MILLISECONDS".equals(batch.getPrecision())) {
            notes.add("业务时间使用毫秒。batch.precision 保持 MILLISECONDS，和 DevicePoints 一致");
        }
        if (batch.isEnabled() && batch.getBufferLimit() < batch.getActions()) {
            notes.add("buffer-limit 小于 actions，网络抖动时失败点不会被重试");
        }
        if (batch.isDropOnQueueExhaustion()) {
            notes.add("队列满时会丢点。只有明确接受丢失时才打开 drop-on-queue-exhaustion");
        }
        if ("ALL".equalsIgnoreCase(properties.getConsistency())) {
            notes.add("consistency=ALL 会把写入延迟放到最慢的那个节点上");
        }
        if ("FULL".equalsIgnoreCase(properties.getLogLevel())) {
            notes.add("log-level=FULL 会把请求体缓存在内存里，排障结束后改回 NONE");
        }
        if (properties.getReplicationFactor() > 1) {
            notes.add("replication-factor 大于 1 只适用于 Enterprise 数据节点，OSS 创建保留策略会失败");
        }
        if (!InfluxSchema.RP_RAW.equals(properties.getRetentionPolicy())) {
            notes.add("当前写入策略不是 rp_raw_7d，降采样连续查询读的是 rp_raw_7d");
        }
        return notes;
    }
}
