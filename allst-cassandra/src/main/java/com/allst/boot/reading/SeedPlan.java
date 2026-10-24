package com.allst.boot.reading;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SeedPlan {

    public static final List<String> DEVICES = List.of("dev-001", "dev-002", "dev-003");
    public static final List<String> METRICS = List.of("temperature", "humidity", "voltage");
    public static final int MINUTES = 60;

    private SeedPlan() {
    }

    public static List<ReadingWriteCommand> commands(Instant now) {
        Instant end = now.truncatedTo(ChronoUnit.MINUTES);
        List<ReadingWriteCommand> commands = new ArrayList<>(DEVICES.size() * METRICS.size() * MINUTES);
        for (int minute = 0; minute < MINUTES; minute++) {
            Instant time = end.minus(Duration.ofMinutes(minute));
            for (String deviceId : DEVICES) {
                for (String metricType : METRICS) {
                    ReadingWriteCommand command = new ReadingWriteCommand();
                    command.setDeviceId(deviceId);
                    command.setRegion("cn-east");
                    command.setMetricType(metricType);
                    command.setValue(value(metricType, minute));
                    command.setStatus(1);
                    command.setTime(time.toString());
                    command.setDeviceModel("gw-1");
                    command.setTags(Map.of("site", "demo"));
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    public static double value(String metricType, int minute) {
        return switch (metricType) {
            case "temperature" -> 20d + (minute % 10);
            case "humidity" -> 40d + (minute % 20);
            case "voltage" -> 220d + (minute % 5);
            default -> throw new IllegalArgumentException("metricType 只允许 temperature、humidity、voltage");
        };
    }
}
