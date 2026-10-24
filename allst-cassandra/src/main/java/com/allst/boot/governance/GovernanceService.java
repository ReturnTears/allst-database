package com.allst.boot.governance;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Instants;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.allst.boot.reading.DayBuckets;
import com.allst.boot.reading.ReadingIds;
import com.allst.boot.reading.ReadingRules;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GovernanceService {

    private final CqlGateway gateway;
    private final CassandraProperties properties;
    private final ZoneId zone;

    public GovernanceService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> advice() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("advice", GovernanceNotes.advice());
        return body;
    }

    public Map<String, Object> deleteRow(String deviceId, String dayText, String metricType, String time, String readingId,
                                         String region) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.requireMetric(metricType);
        Instant recordedAt = Instants.parse(time, zone);
        DayBuckets.requireInside(day, recordedAt, null, zone);
        UUID id = ReadingIds.parse(readingId);
        gateway.execute(gateway.mutation(Statements.deleteRow(properties.getKeyspace()), true)
                .setString("device_id", device)
                .setLocalDate("day_bucket", day)
                .setString("metric_type", metric)
                .setInstant("recorded_at", recordedAt)
                .setUuid("reading_id", id)
                .build());
        List<String> warnings = new ArrayList<>();
        String regionId = Identifiers.blankToNull(region);
        if (regionId == null) {
            warnings.add("没有传 region，reading_by_region 里的对应行没有删，只能等 TTL");
        } else {
            gateway.execute(gateway.mutation(Statements.deleteRegionRow(properties.getKeyspace()), true)
                    .setString("region", Identifiers.requireRegion(regionId))
                    .setLocalDate("day_bucket", day)
                    .setString("metric_type", metric)
                    .setInstant("recorded_at", recordedAt)
                    .setString("device_id", device)
                    .setUuid("reading_id", id)
                    .build());
        }
        return deleted("row", device, day, warnings);
    }

    public Map<String, Object> deleteRange(String deviceId, String dayText, String metricType, String fromText, String toText) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.requireMetric(metricType);
        Instant from = Instants.parse(fromText, zone);
        Instant to = Instants.parse(toText, zone);
        DayBuckets.requireInside(day, from, to, zone);
        gateway.execute(gateway.mutation(Statements.deleteRange(properties.getKeyspace()), true)
                .setString("device_id", device)
                .setLocalDate("day_bucket", day)
                .setString("metric_type", metric)
                .setInstant("range_from", from)
                .setInstant("range_to", to)
                .build());
        return deleted("range", device, day, List.of(
                "这是 sensor_reading 上的范围墓碑",
                "reading_by_region 不能在 recorded_at 是范围的同时再限制 device_id，区域表靠 TTL 过期"
        ));
    }

    public Map<String, Object> deletePartition(String deviceId, String dayText) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        gateway.execute(gateway.mutation(Statements.deletePartition(properties.getKeyspace()), true)
                .setString("device_id", device)
                .setLocalDate("day_bucket", day)
                .build());
        return deleted("partition", device, day, List.of("分区删除只覆盖 sensor_reading。区域表没有按设备分区，靠 TTL 过期。"));
    }

    public void deleteRegionPartition(String region, String dayText) {
        gateway.execute(gateway.mutation(Statements.deleteRegionPartition(properties.getKeyspace()), true)
                .setString("region", Identifiers.requireRegion(region))
                .setLocalDate("day_bucket", ReadingRules.day(dayText))
                .build());
    }

    private Map<String, Object> deleted(String mode, String deviceId, LocalDate day, List<String> warnings) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", mode);
        body.put("deviceId", deviceId);
        body.put("day", day.toString());
        body.put("applied", true);
        body.put("warnings", warnings);
        return body;
    }
}
