package com.allst.es.scenario.common;

import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 与索引日期格式 date_hour_minute_second 对齐：yyyy-MM-dd'T'HH:mm:ss。
 * 接口同时接受空格分隔的 yyyy-MM-dd HH:mm:ss，以及只到日期的 yyyy-MM-dd。
 */
public final class ScenarioTimes {
    public static final DateTimeFormatter ES_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ScenarioTimes() {
    }

    public static LocalDateTime parse(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String value = text.trim().replace(" ", "T");
        try {
            if (value.length() == 10) {
                return LocalDate.parse(value).atStartOfDay();
            }
            if (value.length() == 16) {
                value = value + ":00";
            }
            return LocalDateTime.parse(value, ES_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    public static String format(LocalDateTime time) {
        return time.format(ES_DATE);
    }

    public static LocalDateTime[] window(String from, String to, int defaultDays) {
        LocalDateTime end = parse(to);
        LocalDateTime start = parse(from);
        if (end == null) {
            end = LocalDateTime.now().withNano(0);
        }
        if (start == null) {
            start = end.minusDays(defaultDays);
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        return new LocalDateTime[]{start, end};
    }

    public static String chooseInterval(String requested, LocalDateTime start, LocalDateTime end) {
        if ("hour".equalsIgnoreCase(requested)) {
            return "hour";
        }
        if ("day".equalsIgnoreCase(requested)) {
            return "day";
        }
        if (Duration.between(start, end).toHours() <= 48) {
            return "hour";
        }
        return "day";
    }
}
