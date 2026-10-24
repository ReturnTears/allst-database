package com.allst.boot.reading;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record ValidReading(
        String deviceId,
        String region,
        String metricType,
        LocalDate dayBucket,
        Instant recordedAt,
        UUID readingId,
        double sensorValue,
        Integer status,
        Map<String, String> tags,
        String deviceModel,
        Integer ttlSeconds
) {
}
