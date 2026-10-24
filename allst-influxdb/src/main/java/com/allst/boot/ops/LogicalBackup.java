package com.allst.boot.ops;

import com.allst.boot.influx.common.TimeRanges;
import com.allst.boot.influx.schema.InfluxSchema;
import com.allst.boot.metric.DevicePoints;
import com.allst.boot.metric.ValidMetric;

import java.time.Instant;
import java.util.Map;

public final class LogicalBackup {

    private LogicalBackup() {
    }

    public static String line(Map<String, Object> row) {
        Object rawTime = row.get("time");
        Object rawValue = row.get(InfluxSchema.FIELD_VALUE);
        if (rawTime == null || !(rawValue instanceof Number)) {
            throw new IllegalArgumentException("导出结果缺少 time 或 value");
        }
        Instant time = rawTime instanceof Number
                ? Instant.ofEpochMilli(((Number) rawTime).longValue())
                : TimeRanges.parseInstant(String.valueOf(rawTime));
        ValidMetric metric = new ValidMetric(
                required(row, InfluxSchema.TAG_DEVICE_ID),
                required(row, InfluxSchema.TAG_REGION),
                required(row, InfluxSchema.TAG_METRIC_TYPE),
                ((Number) rawValue).doubleValue(),
                status(row.get(InfluxSchema.FIELD_STATUS)),
                time);
        return DevicePoints.line(DevicePoints.toPoint(metric));
    }

    public static void checkLine(String line) {
        if (line.length() > 65536 || !line.startsWith(InfluxSchema.MEASUREMENT_RAW + ",")) {
            throw new IllegalArgumentException("恢复文件里有不属于 device_metric 的行");
        }
    }

    private static String required(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("导出结果缺少 " + key);
        }
        return String.valueOf(value);
    }

    private static Long status(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("status 不是数值");
        }
        double number = ((Number) value).doubleValue();
        if (number != Math.rint(number)) {
            throw new IllegalArgumentException("status 不是整数，无法写成 line protocol");
        }
        return (long) number;
    }
}
