package com.allst.boot.reading;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReadingGroups {

    private ReadingGroups() {
    }

    public static Map<DeviceDay, List<ValidReading>> byDeviceDay(List<ValidReading> readings) {
        Map<DeviceDay, List<ValidReading>> grouped = new LinkedHashMap<>();
        for (ValidReading reading : readings) {
            grouped.computeIfAbsent(new DeviceDay(reading.deviceId(), reading.dayBucket()), key -> new ArrayList<>()).add(reading);
        }
        return grouped;
    }

    public static Map<RegionDay, List<ValidReading>> byRegionDay(List<ValidReading> readings) {
        Map<RegionDay, List<ValidReading>> grouped = new LinkedHashMap<>();
        for (ValidReading reading : readings) {
            grouped.computeIfAbsent(new RegionDay(reading.region(), reading.dayBucket()), key -> new ArrayList<>()).add(reading);
        }
        return grouped;
    }

    public record DeviceDay(String deviceId, LocalDate dayBucket) {
    }

    public record RegionDay(String region, LocalDate dayBucket) {
    }
}
