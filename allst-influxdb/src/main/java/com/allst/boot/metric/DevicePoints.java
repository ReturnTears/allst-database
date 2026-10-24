package com.allst.boot.metric;

import com.allst.boot.influx.schema.InfluxSchema;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

public final class DevicePoints {

    private DevicePoints() {
    }

    public static Point toPoint(ValidMetric metric) {
        Point.Builder builder = Point.measurement(InfluxSchema.MEASUREMENT_RAW)
                .time(metric.getTime().toEpochMilli(), TimeUnit.MILLISECONDS)
                .tag(InfluxSchema.TAG_DEVICE_ID, metric.getDeviceId())
                .tag(InfluxSchema.TAG_REGION, metric.getRegion())
                .tag(InfluxSchema.TAG_METRIC_TYPE, metric.getMetricType())
                .addField(InfluxSchema.FIELD_VALUE, metric.getValue());
        if (metric.getStatus() != null) {
            builder.addField(InfluxSchema.FIELD_STATUS, metric.getStatus().longValue());
        }
        return builder.build();
    }

    public static String line(Point point) {
        return point.lineProtocol(TimeUnit.NANOSECONDS);
    }
}
