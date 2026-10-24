package com.allst.boot.influx.sql;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * InfluxQL 只接受这里放行的标识符、tag 值和时间。业务参数先校验，再拼进语句。
 */
public final class InfluxQl {

    public static final List<String> WINDOWS = List.of(
            "10s", "30s", "1m", "5m", "10m", "15m", "30m", "1h", "6h", "12h", "1d");

    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}$");
    private static final Pattern TAG_VALUE = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_.:-]{0,63}$");
    private static final Pattern DURATION = Pattern.compile("^([1-9][0-9]{0,4})(s|m|h|d|w)$");
    private static final Pattern TIMESTAMP = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?Z$");
    private static final DateTimeFormatter UTC_MILLIS = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    private InfluxQl() {
    }

    public static String quoteIdent(String name) {
        requireIdent(name);
        return "\"" + name + "\"";
    }

    public static String quoteTag(String value) {
        requireTagValue(value);
        return "'" + value + "'";
    }

    public static String quoteTimestamp(String value) {
        if (value == null || !TIMESTAMP.matcher(value).matches()) {
            throw new IllegalArgumentException("时间必须是 UTC，例如 2026-10-24T00:00:00.000Z");
        }
        return "'" + value + "'";
    }

    public static String format(Instant instant) {
        return UTC_MILLIS.format(instant);
    }

    public static void requireIdent(String name) {
        if (name == null || !IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("标识符只允许字母、数字和下划线，且不能以数字开头: " + name);
        }
    }

    public static void requireTagValue(String value) {
        if (value == null || !TAG_VALUE.matcher(value).matches()) {
            throw new IllegalArgumentException("tag 值只允许字母、数字以及 _ . : - ，且不能为空: " + value);
        }
    }

    public static String requireWindow(String window) {
        if (window == null || !WINDOWS.contains(window.trim().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("聚合窗口只允许 " + String.join("、", WINDOWS));
        }
        return window.trim().toLowerCase(Locale.ROOT);
    }

    public static Duration parseDuration(String text) {
        if (text == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        Matcher matcher = DURATION.matcher(text.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("时间范围格式类似 15m、1h、7d，单位只允许 s、m、h、d、w");
        }
        long amount = Long.parseLong(matcher.group(1));
        switch (matcher.group(2)) {
            case "s":
                return Duration.ofSeconds(amount);
            case "m":
                return Duration.ofMinutes(amount);
            case "h":
                return Duration.ofHours(amount);
            case "d":
                return Duration.ofDays(amount);
            case "w":
                return Duration.ofDays(Math.multiplyExact(amount, 7));
            default:
                throw new IllegalArgumentException("不支持的时间单位");
        }
    }

    public static void appendEquals(StringBuilder sql, String column, String value) {
        if (value == null) {
            return;
        }
        sql.append(" AND ").append(quoteIdent(column)).append(" = ").append(quoteTag(value));
    }

    public static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
