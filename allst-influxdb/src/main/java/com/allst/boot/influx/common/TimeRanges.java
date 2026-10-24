package com.allst.boot.influx.common;

import com.allst.boot.influx.sql.InfluxQl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 查询区间是左闭右开。[start, end)。不带时区的时间按 Asia/Shanghai 解释，带 Z 或偏移的按绝对时间解释。
 */
public final class TimeRanges {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter LOCAL_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Instant EARLIEST = Instant.parse("2000-01-01T00:00:00Z");

    private TimeRanges() {
    }

    public static TimeRange parse(String range, String start, String end, Duration maxSpan) {
        String rangeText = InfluxQl.blankToNull(range);
        String startText = InfluxQl.blankToNull(start);
        String endText = InfluxQl.blankToNull(end);
        if (rangeText != null && (startText != null || endText != null)) {
            throw new IllegalArgumentException("不要同时传 range 和 start/end");
        }
        Instant now = Instant.now();
        Instant rangeStart;
        Instant rangeEnd;
        if (rangeText != null) {
            Duration duration = InfluxQl.parseDuration(rangeText);
            if (duration.compareTo(Duration.ofSeconds(1)) < 0) {
                throw new IllegalArgumentException("时间范围至少 1 秒");
            }
            if (duration.compareTo(maxSpan) > 0) {
                throw new IllegalArgumentException("时间范围不能超过 " + spanText(maxSpan));
            }
            rangeEnd = now;
            rangeStart = now.minus(duration);
        } else if (startText != null && endText != null) {
            rangeStart = parseInstant(startText);
            rangeEnd = parseInstant(endText);
        } else {
            throw new IllegalArgumentException("必须指定 range，或同时指定 start 与 end");
        }
        if (!rangeStart.isBefore(rangeEnd)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
        if (Duration.between(rangeStart, rangeEnd).compareTo(maxSpan) > 0) {
            throw new IllegalArgumentException("时间范围不能超过 " + spanText(maxSpan));
        }
        if (rangeStart.isBefore(EARLIEST)) {
            throw new IllegalArgumentException("开始时间过早");
        }
        if (rangeEnd.isAfter(now.plus(Duration.ofMinutes(5)))) {
            throw new IllegalArgumentException("结束时间不能超前超过 5 分钟");
        }
        String predicate = "time >= " + InfluxQl.quoteTimestamp(InfluxQl.format(rangeStart))
                + " AND time < " + InfluxQl.quoteTimestamp(InfluxQl.format(rangeEnd));
        return new TimeRange(rangeStart, rangeEnd, predicate);
    }

    private static String spanText(Duration maxSpan) {
        long days = maxSpan.toDays();
        if (Duration.ofDays(days).equals(maxSpan)) {
            return days + " 天";
        }
        return maxSpan.toHours() + " 小时";
    }

    public static Instant parseInstant(String text) {
        String value = text.trim();
        try {
            if (value.endsWith("Z")) {
                return Instant.parse(value.contains(" ") ? value.replace(' ', 'T') : value);
            }
            if (value.contains("+") || value.lastIndexOf('-') > 10) {
                return OffsetDateTime.parse(value.contains(" ") ? value.replace(' ', 'T') : value).toInstant();
            }
            if (value.length() == 10) {
                return LocalDate.parse(value, LOCAL_DATE).atStartOfDay(BUSINESS_ZONE).toInstant();
            }
            if (value.contains(" ")) {
                return LocalDateTime.parse(value, LOCAL_DATE_TIME).atZone(BUSINESS_ZONE).toInstant();
            }
            return LocalDateTime.parse(value).atZone(BUSINESS_ZONE).toInstant();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("无法解析时间: " + text);
        }
    }
}
