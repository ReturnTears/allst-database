package com.allst.boot.metric;

import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.common.TimeRanges;
import com.allst.boot.influx.sql.InfluxQl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class MetricRules {

    private MetricRules() {
    }

    public static ValidMetric validate(MetricWriteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String deviceId = requiredTag(command.getDeviceId(), "deviceId");
        String region = requiredTag(command.getRegion(), "region");
        String metricType = requiredTag(command.getMetricType(), "metricType");
        if (command.getValue() == null || !Double.isFinite(command.getValue())
                || command.getValue() < -1_000_000_000_000d || command.getValue() > 1_000_000_000_000d) {
            throw new IllegalArgumentException("value 必须是有限数值");
        }
        Long status = command.getStatus();
        if (status != null && (status < 0 || status > 32767)) {
            throw new IllegalArgumentException("status 必须在 0 到 32767 之间");
        }
        Instant time = command.getTime() == null || command.getTime().isBlank()
                ? Instant.now()
                : TimeRanges.parseInstant(command.getTime());
        Instant now = Instant.now();
        if (time.isAfter(now.plus(InfluxLimits.WRITE_FUTURE_SKEW))) {
            throw new IllegalArgumentException("写入时间不能超前超过 5 分钟");
        }
        if (time.isBefore(now.minus(InfluxLimits.WRITE_PAST_MAX))) {
            throw new IllegalArgumentException("写入时间已经超出原始数据保留期 7 天");
        }
        return new ValidMetric(deviceId, region, metricType, command.getValue(), status, time);
    }

    public static List<ValidMetric> validateBatch(List<MetricWriteCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("points 不能为空");
        }
        if (commands.size() > InfluxLimits.WRITE_BATCH_MAX) {
            throw new IllegalArgumentException("单次最多写入 " + InfluxLimits.WRITE_BATCH_MAX + " 个点");
        }
        List<ValidMetric> metrics = new ArrayList<>(commands.size());
        for (MetricWriteCommand command : commands) {
            metrics.add(validate(command));
        }
        return metrics;
    }

    public static String requiredTag(String value, String label) {
        String text = InfluxQl.blankToNull(value);
        if (text == null) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        InfluxQl.requireTagValue(text);
        return text;
    }

    public static String optionalTag(String value, String label) {
        String text = InfluxQl.blankToNull(value);
        if (text == null) {
            return null;
        }
        InfluxQl.requireTagValue(text);
        return text;
    }
}
