package com.allst.boot.cassandra.common;

public final class Limits {

    public static final int QUERY_DEFAULT = 100;
    public static final int QUERY_MAX = 500;
    public static final int PAGE_DEFAULT = 100;
    public static final int PAGE_MAX = 200;
    public static final int BATCH_MAX = 200;
    public static final int BATCH_CHUNK = 50;
    public static final int WRITE_FUTURE_MINUTES = 5;
    public static final int WRITE_PAST_DAYS = 7;
    public static final int TTL_MAX_SECONDS = 2_592_000;
    public static final int TAGS_MAX = 8;
    public static final int ROLLUP_MAX_ROWS = 5_000;
    public static final int EXPORT_DEFAULT = 1_000;
    public static final int EXPORT_MAX = 5_000;
    public static final long EXPORT_BYTES = 20L * 1024 * 1024;
    public static final int BENCHMARK_DEFAULT = 50;
    public static final int BENCHMARK_MAX = 100;
    public static final int COUNTER_DELTA_MAX = 1_000;
    public static final int STOCK_MAX = 1_000_000;
    public static final int COLLECTION_MAX = 16;

    private Limits() {
    }

    public static int limit(Integer requested, int fallback, int max, String label) {
        int value = requested == null ? fallback : requested;
        if (value < 1 || value > max) {
            throw new IllegalArgumentException(label + "必须在 1 到 " + max + " 之间");
        }
        return value;
    }
}
