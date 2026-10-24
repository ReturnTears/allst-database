package com.allst.boot.reading;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ReadingIds {

    private ReadingIds() {
    }

    public static UUID of(String deviceId, LocalDate day, String metricType, Instant recordedAt) {
        String raw = deviceId + "|" + day + "|" + metricType + "|" + recordedAt.toEpochMilli();
        return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static UUID parse(String text) {
        try {
            return UUID.fromString(text.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("readingId 不是合法的 UUID");
        }
    }
}
