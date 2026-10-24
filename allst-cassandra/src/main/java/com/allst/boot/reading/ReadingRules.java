package com.allst.boot.reading;

import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Instants;
import com.allst.boot.cassandra.common.Limits;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ReadingRules {

    public static final Set<String> METRICS = Set.of("temperature", "humidity", "voltage");

    private ReadingRules() {
    }

    public static ValidReading validate(ReadingWriteCommand command, ZoneId zone, Instant now) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String deviceId = Identifiers.requireId(command.getDeviceId(), "deviceId");
        String region = Identifiers.requireRegion(command.getRegion());
        String metricType = requireMetric(command.getMetricType());
        double sensorValue = requireValue(command.getValue());
        Integer status = optionalStatus(command.getStatus());
        Instant recordedAt = recordedAt(command.getTime(), zone, now);
        LocalDate dayBucket = DayBuckets.of(recordedAt, zone);
        UUID readingId = command.getReadingId() == null || command.getReadingId().isBlank()
                ? ReadingIds.of(deviceId, dayBucket, metricType, recordedAt)
                : ReadingIds.parse(command.getReadingId());
        return new ValidReading(deviceId, region, metricType, dayBucket, recordedAt, readingId, sensorValue, status,
                tags(command.getTags()), Identifiers.optionalModel(command.getDeviceModel()), ttl(command.getTtlSeconds()));
    }

    public static List<ValidReading> validateBatch(List<ReadingWriteCommand> commands, ZoneId zone, Instant now) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("readings 不能为空");
        }
        if (commands.size() > Limits.BATCH_MAX) {
            throw new IllegalArgumentException("单次最多写入 " + Limits.BATCH_MAX + " 行");
        }
        List<ValidReading> readings = new ArrayList<>(commands.size());
        Set<String> keys = new HashSet<>();
        Map<String, String> models = new HashMap<>();
        for (ReadingWriteCommand command : commands) {
            ValidReading reading = validate(command, zone, now);
            String key = reading.deviceId() + "|" + reading.dayBucket() + "|" + reading.metricType()
                    + "|" + reading.recordedAt().toEpochMilli() + "|" + reading.readingId();
            if (!keys.add(key)) {
                throw new IllegalArgumentException("同一批次里主键重复");
            }
            if (reading.deviceModel() != null) {
                String partition = reading.deviceId() + "|" + reading.dayBucket();
                String previous = models.putIfAbsent(partition, reading.deviceModel());
                if (previous != null && !previous.equals(reading.deviceModel())) {
                    throw new IllegalArgumentException("同一分区的 deviceModel 必须相同");
                }
            }
            readings.add(reading);
        }
        return readings;
    }

    public static String requireMetric(String value) {
        String text = Identifiers.blankToNull(value);
        if (text == null) {
            throw new IllegalArgumentException("metricType 不能为空");
        }
        String metric = text.toLowerCase(Locale.ROOT);
        if (!METRICS.contains(metric)) {
            throw new IllegalArgumentException("metricType 只允许 temperature、humidity、voltage");
        }
        return metric;
    }

    public static String optionalMetric(String value) {
        return value == null || value.isBlank() ? null : requireMetric(value);
    }

    public static boolean ascending(String order) {
        if (order == null || order.isBlank() || "desc".equalsIgnoreCase(order)) {
            return false;
        }
        if ("asc".equalsIgnoreCase(order)) {
            return true;
        }
        throw new IllegalArgumentException("order 只允许 asc 或 desc");
    }

    public static LocalDate day(String value) {
        if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("day 格式为 yyyy-MM-dd");
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("day 不是合法日期");
        }
    }

    private static double requireValue(Double value) {
        if (value == null || !Double.isFinite(value) || value < -1_000_000d || value > 1_000_000d) {
            throw new IllegalArgumentException("value 必须是 -1000000 到 1000000 之间的有限数值");
        }
        return value;
    }

    private static Integer optionalStatus(Integer status) {
        if (status == null) {
            return null;
        }
        if (status < 0 || status > 9) {
            throw new IllegalArgumentException("status 必须在 0 到 9 之间");
        }
        return status;
    }

    private static Integer ttl(Integer ttlSeconds) {
        if (ttlSeconds == null) {
            return null;
        }
        if (ttlSeconds < 1 || ttlSeconds > Limits.TTL_MAX_SECONDS) {
            throw new IllegalArgumentException("ttlSeconds 必须在 1 到 " + Limits.TTL_MAX_SECONDS + " 之间，0 会取消表默认 TTL");
        }
        return ttlSeconds;
    }

    private static Instant recordedAt(String text, ZoneId zone, Instant now) {
        Instant recordedAt = (text == null || text.isBlank() ? now : Instants.parse(text, zone)).truncatedTo(ChronoUnit.MILLIS);
        if (recordedAt.isAfter(now.plus(Duration.ofMinutes(Limits.WRITE_FUTURE_MINUTES)))) {
            throw new IllegalArgumentException("写入时间不能超前超过 5 分钟");
        }
        if (recordedAt.isBefore(now.minus(Duration.ofDays(Limits.WRITE_PAST_DAYS)))) {
            throw new IllegalArgumentException("写入时间已经超出原始数据保留期 7 天");
        }
        return recordedAt;
    }

    private static Map<String, String> tags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Map.of();
        }
        if (tags.size() > Limits.TAGS_MAX) {
            throw new IllegalArgumentException("tags 最多 " + Limits.TAGS_MAX + " 个");
        }
        Map<String, String> copy = new LinkedHashMap<>();
        tags.forEach((key, value) -> {
            Identifiers.requireTagKey(key);
            if (value == null || value.length() > 64 || value.chars().anyMatch(ch -> ch < 32)) {
                throw new IllegalArgumentException("tag 的值最长 64 个字符，并且不能包含控制字符");
            }
            copy.put(key, value);
        });
        return Map.copyOf(copy);
    }
}
