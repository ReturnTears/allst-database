package com.allst.boot.metric;

import java.time.Instant;

public final class ValidMetric {

    private final String deviceId;
    private final String region;
    private final String metricType;
    private final double value;
    private final Long status;
    private final Instant time;

    public ValidMetric(String deviceId, String region, String metricType, double value, Long status, Instant time) {
        this.deviceId = deviceId;
        this.region = region;
        this.metricType = metricType;
        this.value = value;
        this.status = status;
        this.time = time;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getRegion() {
        return region;
    }

    public String getMetricType() {
        return metricType;
    }

    public double getValue() {
        return value;
    }

    public Long getStatus() {
        return status;
    }

    public Instant getTime() {
        return time;
    }
}
