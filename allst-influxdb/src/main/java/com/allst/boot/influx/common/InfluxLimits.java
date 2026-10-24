package com.allst.boot.influx.common;

import java.time.Duration;

public final class InfluxLimits {

    public static final int WRITE_BATCH_MAX = 1000;
    public static final int RAW_LIMIT_DEFAULT = 100;
    public static final int RAW_LIMIT_MAX = 500;
    public static final int ANALYTICS_LIMIT_DEFAULT = 2000;
    public static final int ANALYTICS_LIMIT_MAX = 10000;
    public static final int SERIES_LIMIT = 20;
    public static final int BENCHMARK_MAX = 5000;
    public static final int EXPORT_DEFAULT = 5000;
    public static final int EXPORT_MAX = 20000;
    public static final long EXPORT_BYTES = 20L * 1024 * 1024;
    public static final Duration RAW_MAX_SPAN = Duration.ofDays(7);
    public static final Duration ANALYTICS_MAX_SPAN = Duration.ofDays(7);
    public static final Duration WRITE_FUTURE_SKEW = Duration.ofMinutes(5);
    public static final Duration WRITE_PAST_MAX = Duration.ofDays(7);

    private InfluxLimits() {
    }
}
