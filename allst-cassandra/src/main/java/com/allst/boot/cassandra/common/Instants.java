package com.allst.boot.cassandra.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class Instants {

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Instants() {
    }

    public static Instant parse(String text, ZoneId zone) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("时间不能为空");
        }
        String value = text.trim();
        try {
            if (value.contains("T")) {
                return Instant.parse(value);
            }
            return LocalDateTime.parse(value, LOCAL).atZone(zone).toInstant();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("时间格式用 2026-10-24T00:00:00Z 或 2026-10-24 08:00:00");
        }
    }
}
