package com.allst.boot.ops;

import com.allst.boot.cassandra.common.CassandraAccessException;
import com.allst.boot.cassandra.common.Chunks;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.reading.ReadingQueryService;
import com.allst.boot.reading.ReadingRules;
import com.allst.boot.reading.ReadingWriteCommand;
import com.allst.boot.reading.ReadingWriteService;
import com.allst.boot.reading.ValidReading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackupService {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final CassandraProperties properties;
    private final ReadingQueryService queryService;
    private final ReadingWriteService writeService;
    private final ObjectMapper objectMapper;
    private final ZoneId zone;

    public BackupService(CassandraProperties properties, ReadingQueryService queryService,
                         ReadingWriteService writeService, ObjectMapper objectMapper) {
        this.properties = properties;
        this.queryService = queryService;
        this.writeService = writeService;
        this.objectMapper = objectMapper;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> plan(String tag) {
        String snapshot = tag == null || tag.isBlank()
                ? "allst-" + FILE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))
                : tag;
        return BackupCommands.plan(properties.getKeyspace(), snapshot);
    }

    public Map<String, Object> export(String deviceId, String day, String metricType, Integer limit) {
        int rowLimit = Limits.limit(limit, Limits.EXPORT_DEFAULT, Limits.EXPORT_MAX, "limit");
        List<Map<String, Object>> rows = queryService.exportRows(deviceId, day, metricType, rowLimit);
        List<String> lines = new ArrayList<>();
        lines.add("# keyspace=" + properties.getKeyspace());
        lines.add("# table=sensor_reading");
        lines.add("# mode=partition");
        for (Map<String, Object> row : rows) {
            lines.add(writeJson(toCommand(row)));
        }
        String fileName = "sensor-reading-" + FILE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)) + ".jsonl";
        Path file = BackupPaths.resolveFile(BackupPaths.root(properties.getOps().getBackupDir()), fileName);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new CassandraAccessException(false, "写入备份文件失败: " + ex.getMessage(), ex);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", "logical-partition");
        body.put("fileName", fileName);
        body.put("directory", file.getParent().toString());
        body.put("rows", rows.size());
        body.put("truncated", rows.size() == rowLimit);
        body.put("sha256", sha256(file));
        body.put("hint", "这是一个分区的逻辑导出。恢复时会按现在的时间重新写入，不保留原来的 writetime，也不能恢复已经删除的墓碑。");
        return body;
    }

    public Map<String, Object> restore(String fileName) {
        Path file = BackupPaths.resolveFile(BackupPaths.root(properties.getOps().getBackupDir()), fileName);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("备份文件不存在: " + fileName);
        }
        List<String> lines;
        try {
            if (Files.size(file) > Limits.EXPORT_BYTES) {
                throw new IllegalArgumentException("备份文件超过 20MB，请按分区拆开");
            }
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new CassandraAccessException(false, "读取备份文件失败: " + ex.getMessage(), ex);
        }
        List<ReadingWriteCommand> commands = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (commands.size() >= Limits.EXPORT_MAX) {
                throw new IllegalArgumentException("备份文件超过 " + Limits.EXPORT_MAX + " 行");
            }
            commands.add(readCommand(line));
        }
        Instant now = Instant.now();
        int written = 0;
        for (List<ReadingWriteCommand> chunk : Chunks.split(commands, Limits.BATCH_MAX)) {
            List<ValidReading> readings = ReadingRules.validateBatch(chunk, zone, now);
            writeService.writeValidated(readings, true);
            written += readings.size();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fileName", fileName);
        body.put("rows", written);
        body.put("hint", "逻辑恢复走的是正常插入。表默认 TTL 会从恢复这一刻重新计算。");
        return body;
    }

    private Map<String, Object> toCommand(Map<String, Object> row) {
        Map<String, Object> command = new LinkedHashMap<>();
        command.put("deviceId", row.get("device_id"));
        command.put("region", row.get("region"));
        command.put("metricType", row.get("metric_type"));
        command.put("value", row.get("sensor_value"));
        command.put("status", row.get("status"));
        command.put("time", row.get("recorded_at") == null ? null : row.get("recorded_at").toString());
        command.put("readingId", row.get("reading_id") == null ? null : row.get("reading_id").toString());
        command.put("deviceModel", row.get("device_model"));
        command.put("tags", row.get("tags"));
        return command;
    }

    private String writeJson(Map<String, Object> command) {
        try {
            return objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException ex) {
            throw new CassandraAccessException(false, "序列化备份行失败", ex);
        }
    }

    private ReadingWriteCommand readCommand(String line) {
        try {
            return objectMapper.readValue(line, ReadingWriteCommand.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("备份行不是合法的 JSON");
        }
    }

    private static String sha256(Path file) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new CassandraAccessException(false, "计算备份摘要失败", ex);
        }
    }
}
