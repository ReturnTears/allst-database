package com.allst.boot.reading;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class DayBuckets {

    private DayBuckets() {
    }

    public static LocalDate of(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate();
    }

    public static void requireInside(LocalDate day, Instant from, Instant to, ZoneId zone) {
        Instant start = day.atStartOfDay(zone).toInstant();
        Instant end = day.plusDays(1).atStartOfDay(zone).toInstant();
        if (from != null && (from.isBefore(start) || !from.isBefore(end))) {
            throw new IllegalArgumentException("from 必须落在 day 这一个日桶里");
        }
        if (to != null && (!to.isAfter(start) || to.isAfter(end))) {
            throw new IllegalArgumentException("to 必须落在 day 这一个日桶里，下一天零点作为右开边界可以");
        }
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("from 必须早于 to");
        }
    }
}
