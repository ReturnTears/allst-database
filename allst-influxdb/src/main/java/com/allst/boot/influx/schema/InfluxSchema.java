package com.allst.boot.influx.schema;

import java.util.List;

/**
 * 设备指标的库表约定。measurement 按业务域命名，tag 只放会用来过滤的低基数字段。
 */
public final class InfluxSchema {

    public static final String MEASUREMENT_RAW = "device_metric";
    public static final String MEASUREMENT_1M = "device_metric_1m";
    public static final String MEASUREMENT_1H = "device_metric_1h";

    public static final String RP_RAW = "rp_raw_7d";
    public static final String RP_90D = "rp_down_90d";
    public static final String RP_365D = "rp_down_365d";

    public static final String CQ_1M = "cq_device_metric_1m";
    public static final String CQ_1H = "cq_device_metric_1h";

    public static final String TAG_DEVICE_ID = "device_id";
    public static final String TAG_REGION = "region";
    public static final String TAG_METRIC_TYPE = "metric_type";

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_MAX = "max_value";
    public static final String FIELD_MIN = "min_value";
    public static final String FIELD_COUNT = "sample_count";

    private InfluxSchema() {
    }

    public static List<RetentionSpec> retentions() {
        return List.of(
                new RetentionSpec(RP_RAW, "7d", "1d", true),
                new RetentionSpec(RP_90D, "90d", "7d", false),
                new RetentionSpec(RP_365D, "365d", "30d", false)
        );
    }

    public static final class RetentionSpec {
        private final String name;
        private final String duration;
        private final String shardDuration;
        private final boolean defaultPolicy;

        public RetentionSpec(String name, String duration, String shardDuration, boolean defaultPolicy) {
            this.name = name;
            this.duration = duration;
            this.shardDuration = shardDuration;
            this.defaultPolicy = defaultPolicy;
        }

        public String getName() {
            return name;
        }

        public String getDuration() {
            return duration;
        }

        public String getShardDuration() {
            return shardDuration;
        }

        public boolean isDefaultPolicy() {
            return defaultPolicy;
        }
    }
}
