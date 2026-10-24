package com.allst.boot.influx.common;

import java.time.Instant;

public final class TimeRange {

    private final Instant start;
    private final Instant end;
    private final String predicate;

    TimeRange(Instant start, Instant end, String predicate) {
        this.start = start;
        this.end = end;
        this.predicate = predicate;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public String predicate() {
        return predicate;
    }
}
