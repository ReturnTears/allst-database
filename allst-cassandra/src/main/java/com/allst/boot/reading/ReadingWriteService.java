package com.allst.boot.reading;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.Chunks;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReadingWriteService {

    private static final Logger log = LoggerFactory.getLogger(ReadingWriteService.class);

    private final CqlGateway gateway;
    private final CassandraProperties properties;
    private final ZoneId zone;

    public ReadingWriteService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> write(ReadingWriteCommand command) {
        ValidReading reading = ReadingRules.validate(command, zone, java.time.Instant.now());
        writeValidated(List.of(reading), true);
        return receipt(List.of(reading), true);
    }

    public Map<String, Object> writeBatch(ReadingBatchCommand command) {
        List<ValidReading> readings = ReadingRules.validateBatch(command == null ? null : command.getReadings(), zone,
                java.time.Instant.now());
        writeValidated(readings, true);
        return receipt(readings, true);
    }

    public Map<String, Object> writeAsync(ReadingWriteCommand command) {
        ValidReading reading = ReadingRules.validate(command, zone, java.time.Instant.now());
        writeValidated(List.of(reading), false);
        Map<String, Object> body = receipt(List.of(reading), false);
        body.put("hint", "异步接口返回时协调节点还没有确认。进程退出会丢未完成的写入，业务确认用同步接口。");
        return body;
    }

    public Map<String, Object> seed() {
        java.time.Instant now = java.time.Instant.now();
        List<ValidReading> readings = new ArrayList<>();
        for (ReadingWriteCommand command : SeedPlan.commands(now)) {
            readings.add(ReadingRules.validate(command, zone, now));
        }
        for (List<ValidReading> chunk : Chunks.split(readings, Limits.BATCH_MAX)) {
            writeValidated(chunk, true);
        }
        log.info("写入样例 {} 行", readings.size());
        Map<String, Object> body = receipt(readings, true);
        body.put("devices", SeedPlan.DEVICES);
        body.put("metrics", SeedPlan.METRICS);
        body.put("minutes", SeedPlan.MINUTES);
        body.put("hint", "同一分钟内重复调用会覆盖这批主键，不会越积越多。reading_id 由设备、日桶、指标和时间戳生成。");
        return body;
    }

    public void writeValidated(List<ValidReading> readings, boolean wait) {
        String keyspace = properties.getKeyspace();
        List<Statement<?>> statements = new ArrayList<>();
        ReadingGroups.byDeviceDay(readings).values().forEach(group -> statements.addAll(readingBatches(keyspace, group)));
        ReadingGroups.byRegionDay(readings).values().forEach(group -> statements.addAll(regionBatches(keyspace, group)));
        statements.addAll(modelStatements(keyspace, readings));
        gateway.executeEach(statements, wait);
    }

    private List<Statement<?>> readingBatches(String keyspace, List<ValidReading> readings) {
        List<Statement<?>> statements = new ArrayList<>();
        for (List<ValidReading> chunk : Chunks.split(readings, Limits.BATCH_CHUNK)) {
            List<BoundStatement> bound = new ArrayList<>(chunk.size());
            for (ValidReading reading : chunk) {
                bound.add(bindReading(keyspace, reading));
            }
            statements.add(unlogged(bound));
        }
        return statements;
    }

    private List<Statement<?>> regionBatches(String keyspace, List<ValidReading> readings) {
        List<Statement<?>> statements = new ArrayList<>();
        for (List<ValidReading> chunk : Chunks.split(readings, Limits.BATCH_CHUNK)) {
            List<BoundStatement> bound = new ArrayList<>(chunk.size());
            for (ValidReading reading : chunk) {
                bound.add(bindRegion(keyspace, reading));
            }
            statements.add(unlogged(bound));
        }
        return statements;
    }

    private List<Statement<?>> modelStatements(String keyspace, List<ValidReading> readings) {
        Map<ReadingGroups.DeviceDay, String> models = new LinkedHashMap<>();
        for (ValidReading reading : readings) {
            if (reading.deviceModel() != null) {
                models.putIfAbsent(new ReadingGroups.DeviceDay(reading.deviceId(), reading.dayBucket()), reading.deviceModel());
            }
        }
        List<Statement<?>> statements = new ArrayList<>();
        models.forEach((partition, model) -> {
            BoundStatementBuilder builder = gateway.mutation(Statements.insertDeviceModel(keyspace), true)
                    .setString("device_id", partition.deviceId())
                    .setLocalDate("day_bucket", partition.dayBucket())
                    .setString("device_model", model);
            statements.add(builder.build());
        });
        return statements;
    }

    private BoundStatement bindReading(String keyspace, ValidReading reading) {
        boolean ttl = reading.ttlSeconds() != null;
        BoundStatementBuilder builder = gateway.mutation(Statements.insertReading(keyspace, ttl), true);
        bindCommon(builder, reading);
        if (reading.tags().isEmpty()) {
            builder = builder.unset("tags");
        } else {
            builder = builder.setMap("tags", reading.tags(), String.class, String.class);
        }
        if (ttl) {
            builder = builder.setInt("ttl", reading.ttlSeconds());
        }
        return builder.build();
    }

    private BoundStatement bindRegion(String keyspace, ValidReading reading) {
        boolean ttl = reading.ttlSeconds() != null;
        BoundStatementBuilder builder = gateway.mutation(Statements.insertByRegion(keyspace, ttl), true);
        bindCommon(builder, reading);
        if (ttl) {
            builder = builder.setInt("ttl", reading.ttlSeconds());
        }
        return builder.build();
    }

    private static void bindCommon(BoundStatementBuilder builder, ValidReading reading) {
        builder.setString("device_id", reading.deviceId())
                .setLocalDate("day_bucket", reading.dayBucket())
                .setString("metric_type", reading.metricType())
                .setInstant("recorded_at", reading.recordedAt())
                .setUuid("reading_id", reading.readingId())
                .setString("region", reading.region())
                .setDouble("sensor_value", reading.sensorValue());
        if (reading.status() == null) {
            builder.unset("status");
        } else {
            builder.setInt("status", reading.status());
        }
    }

    private Statement<?> unlogged(List<BoundStatement> statements) {
        if (statements.size() == 1) {
            return statements.get(0);
        }
        BatchStatementBuilder builder = BatchStatement.builder(DefaultBatchType.UNLOGGED)
                .setConsistencyLevel(gateway.consistency())
                .setIdempotence(true)
                .setTimeout(gateway.timeout());
        for (BoundStatement statement : statements) {
            builder.addStatement(statement);
        }
        return builder.build();
    }

    private Map<String, Object> receipt(List<ValidReading> readings, boolean confirmed) {
        ValidReading first = readings.get(0);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("confirmed", confirmed);
        body.put("count", readings.size());
        body.put("deviceId", first.deviceId());
        body.put("day", first.dayBucket().toString());
        body.put("readingId", first.readingId().toString());
        body.put("tables", List.of(properties.getKeyspace() + ".sensor_reading", properties.getKeyspace() + ".reading_by_region"));
        body.put("batch", "UNLOGGED，只把同一个分区的行放在一起");
        return body;
    }
}
