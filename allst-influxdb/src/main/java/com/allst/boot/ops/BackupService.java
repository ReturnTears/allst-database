package com.allst.boot.ops;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.InfluxAccessException;
import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.config.InfluxProperties;
import com.allst.boot.influx.schema.InfluxSchema;
import com.allst.boot.metric.MetricQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final int RESTORE_CHUNK = 500;

    private final InfluxGateway gateway;
    private final InfluxProperties properties;
    private final MetricQueryService queryService;

    public BackupService(InfluxGateway gateway, InfluxProperties properties, MetricQueryService queryService) {
        this.gateway = gateway;
        this.properties = properties;
        this.queryService = queryService;
    }

    public Map<String, Object> export(String deviceId, String region, String metricType,
                                      String range, String start, String end, Integer limit) {
        int safeLimit = limit == null ? InfluxLimits.EXPORT_DEFAULT : limit;
        if (safeLimit < 1 || safeLimit > InfluxLimits.EXPORT_MAX) {
            throw new IllegalArgumentException("limit 必须在 1 到 " + InfluxLimits.EXPORT_MAX + " 之间");
        }
        List<Map<String, Object>> rows = queryService.exportRows(deviceId, region, metricType, range, start, end, safeLimit);
        List<String> lines = new ArrayList<>();
        lines.add("# database=" + properties.getDatabase());
        lines.add("# retention=" + properties.getRetentionPolicy());
        lines.add("# measurement=" + InfluxSchema.MEASUREMENT_RAW);
        for (Map<String, Object> row : rows) {
            lines.add(LogicalBackup.line(row));
        }
        String fileName = InfluxSchema.MEASUREMENT_RAW + "-" + FILE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)) + ".lp";
        Path file = BackupPaths.resolveFile(backupRoot(), fileName);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new InfluxAccessException("写入备份文件失败: " + ex.getMessage(), ex, false);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", "logical-partial");
        body.put("fileName", fileName);
        body.put("directory", file.getParent().toString());
        body.put("points", rows.size());
        body.put("truncated", rows.size() == safeLimit);
        body.put("sha256", sha256(file));
        body.put("hint", "这是按条件导出的 line protocol，不能代替 influxd backup -portable");
        return body;
    }

    public Map<String, Object> restore(String fileName) {
        Path file = BackupPaths.resolveFile(backupRoot(), fileName);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("备份文件不存在: " + fileName);
        }
        try {
            if (Files.size(file) > InfluxLimits.EXPORT_BYTES) {
                throw new IllegalArgumentException("备份文件超过 20MB，请拆分后再恢复");
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<String> points = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                LogicalBackup.checkLine(line);
                points.add(line);
            }
            int batches = 0;
            for (int offset = 0; offset < points.size(); offset += RESTORE_CHUNK) {
                int end = Math.min(offset + RESTORE_CHUNK, points.size());
                gateway.writeLines(points.subList(offset, end));
                batches++;
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("fileName", fileName);
            body.put("points", points.size());
            body.put("batches", batches);
            body.put("database", properties.getDatabase());
            body.put("retentionPolicy", properties.getRetentionPolicy());
            return body;
        } catch (IOException ex) {
            throw new InfluxAccessException("读取备份文件失败: " + ex.getMessage(), ex, false);
        }
    }

    public Map<String, Object> portable(String action, String name, String targetDatabase) {
        String safeAction = action == null ? "" : action.trim().toLowerCase(Locale.ROOT);
        Path root = backupRoot();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new InfluxAccessException("创建备份目录失败: " + ex.getMessage(), ex, false);
        }
        InfluxProperties.Ops ops = properties.getOps();
        String stamp = FILE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
        List<String> command;
        Path path;
        if ("backup".equals(safeAction)) {
            String directoryName = name == null || name.isBlank() ? "portable-" + stamp : name;
            path = BackupPaths.resolveDirectory(root, directoryName);
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                throw new InfluxAccessException("创建备份目录失败: " + ex.getMessage(), ex, false);
            }
            command = PortableBackupCommands.backup(ops.getInfluxdCommand(), ops.getBackupHost(),
                    properties.getDatabase(), properties.getRetentionPolicy(), path);
        } else if ("restore".equals(safeAction)) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("恢复必须指定 name，值为备份目录名");
            }
            path = BackupPaths.resolveDirectory(root, name);
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("备份目录不存在: " + name);
            }
            String target = targetDatabase == null || targetDatabase.isBlank()
                    ? PortableBackupCommands.defaultTarget(properties.getDatabase())
                    : targetDatabase.trim();
            command = PortableBackupCommands.restore(ops.getInfluxdCommand(), ops.getBackupHost(),
                    properties.getDatabase(), properties.getRetentionPolicy(), target, path);
        } else {
            throw new IllegalArgumentException("action 只允许 backup 或 restore");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("action", safeAction);
        body.put("command", command);
        body.put("path", path.toString());
        if (!ops.isAllowShell()) {
            body.put("executed", false);
            body.put("reason", "influx.ops.allow-shell 为 false。请在数据库宿主机执行 command，或使用 deploy/backup 脚本");
            return body;
        }
        body.put("executed", true);
        body.put("output", run(command, root));
        return body;
    }

    private Path backupRoot() {
        return Path.of(properties.getOps().getBackupDir());
    }

    private static String run(List<String> command, Path directory) {
        Path logFile = directory.resolve("portable-last.log");
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory.toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(logFile.toFile());
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new InfluxAccessException("influxd 执行超过 120 秒，已终止。大库请改用宿主机脚本", null, false);
            }
            String output = "";
            if (Files.exists(logFile)) {
                String text = Files.readString(logFile);
                output = text.length() > 4000 ? text.substring(text.length() - 4000) : text;
            }
            if (process.exitValue() != 0) {
                throw new InfluxAccessException("influxd 退出码 " + process.exitValue() + ": " + output, null, false);
            }
            log.info("influxd 执行完成: {}", command.get(1));
            return output;
        } catch (IOException ex) {
            throw new InfluxAccessException("无法启动 influxd: " + ex.getMessage(), ex, false);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InfluxAccessException("influxd 执行被中断", ex, false);
        }
    }

    private static String sha256(Path file) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(Character.forDigit((value >> 4) & 0xF, 16));
                hex.append(Character.forDigit(value & 0xF, 16));
            }
            return hex.toString();
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new InfluxAccessException("计算备份校验值失败", ex, false);
        }
    }
}
