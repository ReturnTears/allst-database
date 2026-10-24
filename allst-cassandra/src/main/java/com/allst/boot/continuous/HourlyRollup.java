package com.allst.boot.continuous;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HourlyRollup {

    private HourlyRollup() {
    }

    public static Map<Key, Aggregate> accumulate(List<Point> points, ZoneId zone) {
        Map<Key, Aggregate> buckets = new LinkedHashMap<>();
        for (Point point : points) {
            Instant hour = point.recordedAt().atZone(zone).truncatedTo(ChronoUnit.HOURS).toInstant();
            buckets.computeIfAbsent(new Key(point.metricType(), hour), key -> new Aggregate()).add(point.value());
        }
        return buckets;
    }

    public record Point(String metricType, Instant recordedAt, double value) {
    }

    public record Key(String metricType, Instant hourBucket) {
    }

    public static final class Aggregate {
        private int count;
        private double sum;
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;

        public void add(double value) {
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        public int count() {
            return count;
        }

        public double sum() {
            return sum;
        }

        public double min() {
            return min;
        }

        public double max() {
            return max;
        }
    }
}
