package com.allst.boot.influx.config;

import com.allst.boot.influx.schema.InfluxSchema;
import com.allst.boot.influx.sql.InfluxQl;
import okhttp3.OkHttpClient;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(InfluxProperties.class)
public class InfluxClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(InfluxClientConfiguration.class);
    private static final List<String> PRECISIONS = List.of(
            "NANOSECONDS", "MICROSECONDS", "MILLISECONDS", "SECONDS");

    @Bean(destroyMethod = "close")
    public InfluxDB influxDB(InfluxProperties properties) {
        validate(properties);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout())
                .retryOnConnectionFailure(true);
        InfluxDB influxDB = connect(properties, httpClient);
        influxDB.setDatabase(properties.getDatabase());
        influxDB.setRetentionPolicy(properties.getRetentionPolicy());
        influxDB.setConsistency(consistency(properties));
        influxDB.setLogLevel(InfluxDB.LogLevel.valueOf(properties.getLogLevel().trim().toUpperCase()));
        if (properties.isGzip()) {
            influxDB.enableGzip();
        }
        if (properties.getBatch().isEnabled()) {
            influxDB.enableBatch(batchOptions(properties));
        }
        if (!InfluxSchema.RP_RAW.equals(properties.getRetentionPolicy())) {
            log.warn("写入保留策略是 {}，规范里的原始数据策略是 {}", properties.getRetentionPolicy(), InfluxSchema.RP_RAW);
        }
        return influxDB;
    }

    private static InfluxDB connect(InfluxProperties properties, OkHttpClient.Builder httpClient) {
        if (properties.getUsername() == null || properties.getUsername().isBlank()) {
            return InfluxDBFactory.connect(properties.getUrl(), httpClient);
        }
        return InfluxDBFactory.connect(properties.getUrl(), properties.getUsername(), properties.getPassword(), httpClient);
    }

    private static void validate(InfluxProperties properties) {
        if (properties.getUrl() == null
                || !(properties.getUrl().startsWith("http://") || properties.getUrl().startsWith("https://"))) {
            throw new IllegalStateException("influx.url 必须以 http:// 或 https:// 开头");
        }
        InfluxQl.requireIdent(properties.getDatabase());
        InfluxQl.requireIdent(properties.getRetentionPolicy());
        consistency(properties);
        try {
            InfluxDB.LogLevel.valueOf(properties.getLogLevel().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("influx.log-level 只允许 NONE、BASIC、HEADERS、FULL");
        }
        if (properties.getReplicationFactor() < 1 || properties.getReplicationFactor() > 3) {
            throw new IllegalStateException("influx.replication-factor 只允许 1 到 3");
        }
        InfluxProperties.Batch batch = properties.getBatch();
        if (batch.getActions() < 1 || batch.getFlushIntervalMs() < 1 || batch.getJitterIntervalMs() < 0) {
            throw new IllegalStateException("influx.batch 的 actions 和 flush-interval-ms 必须大于 0");
        }
        if (!PRECISIONS.contains(batch.getPrecision())) {
            throw new IllegalStateException("influx.batch.precision 只允许 " + String.join("、", PRECISIONS));
        }
        if (batch.isEnabled() && batch.getBufferLimit() < batch.getActions()) {
            log.warn("buffer-limit 小于 actions，失败批次不会进入重试缓冲");
        }
        if (properties.getConnectTimeout().isZero() || properties.getConnectTimeout().isNegative()
                || properties.getReadTimeout().isZero() || properties.getReadTimeout().isNegative()
                || properties.getWriteTimeout().isZero() || properties.getWriteTimeout().isNegative()) {
            throw new IllegalStateException("InfluxDB 超时必须大于 0");
        }
    }

    public static InfluxDB.ConsistencyLevel consistency(InfluxProperties properties) {
        try {
            return InfluxDB.ConsistencyLevel.valueOf(properties.getConsistency().trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("influx.consistency 只允许 ALL、ANY、ONE、QUORUM");
        }
    }

    private static BatchOptions batchOptions(InfluxProperties properties) {
        InfluxProperties.Batch batch = properties.getBatch();
        return BatchOptions.DEFAULTS
                .actions(batch.getActions())
                .flushDuration(batch.getFlushIntervalMs())
                .jitterDuration(batch.getJitterIntervalMs())
                .bufferLimit(batch.getBufferLimit())
                .precision(TimeUnit.valueOf(batch.getPrecision()))
                .consistency(consistency(properties))
                .dropActionsOnQueueExhaustion(batch.isDropOnQueueExhaustion())
                .threadFactory(runnable -> {
                    Thread thread = new Thread(runnable, "influx-batch");
                    thread.setDaemon(false);
                    return thread;
                })
                .exceptionHandler((points, throwable) -> log.error("InfluxDB 批量写入失败", throwable))
                .droppedActionHandler(point -> log.warn("InfluxDB 批量队列已满，丢弃一个点"));
    }
}
