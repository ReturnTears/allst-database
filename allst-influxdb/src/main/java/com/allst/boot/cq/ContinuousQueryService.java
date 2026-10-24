package com.allst.boot.cq;

import com.allst.boot.influx.client.InfluxGateway;
import com.allst.boot.influx.common.InfluxLimits;
import com.allst.boot.influx.common.InfluxResults;
import com.allst.boot.influx.common.TimeRange;
import com.allst.boot.influx.common.TimeRanges;
import com.allst.boot.influx.config.InfluxProperties;
import com.allst.boot.influx.schema.InfluxSchema;
import com.allst.boot.influx.schema.SchemaStep;
import com.allst.boot.influx.sql.InfluxQl;
import com.allst.boot.influx.sql.SchemaStatements;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContinuousQueryService {

    private final InfluxGateway gateway;
    private final InfluxProperties properties;

    public ContinuousQueryService(InfluxGateway gateway, InfluxProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : InfluxResults.rows(gateway.command(SchemaStatements.showContinuousQueries()))) {
            if (properties.getDatabase().equals(String.valueOf(row.get("measurement")))) {
                rows.add(row);
            }
        }
        return rows;
    }

    public List<SchemaStep> ensure() {
        List<String> names = new ArrayList<>();
        for (Map<String, Object> row : list()) {
            Object name = row.get("name");
            if (name != null) {
                names.add(String.valueOf(name));
            }
        }
        List<SchemaStep> steps = new ArrayList<>();
        steps.add(createIfMissing(names, InfluxSchema.CQ_1M,
                SchemaStatements.minuteContinuousQuery(properties.getDatabase())));
        steps.add(createIfMissing(names, InfluxSchema.CQ_1H,
                SchemaStatements.hourContinuousQuery(properties.getDatabase())));
        return steps;
    }

    public Map<String, Object> drop(String name) {
        String sql = SchemaStatements.dropContinuousQuery(properties.getDatabase(), name);
        gateway.command(sql);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dropped", name);
        body.put("influxql", sql);
        return body;
    }

    public Map<String, Object> backfill(String level, String range, String start, String end) {
        String safeLevel = normalizeLevel(level);
        Duration window = "1h".equals(safeLevel) ? Duration.ofHours(1) : Duration.ofMinutes(1);
        TimeRange timeRange = closedRange(range, start, end, window);
        String sql = "1h".equals(safeLevel)
                ? SchemaStatements.backfill(InfluxSchema.RP_365D, InfluxSchema.MEASUREMENT_1H, "1h", timeRange.predicate())
                : SchemaStatements.backfill(InfluxSchema.RP_90D, InfluxSchema.MEASUREMENT_1M, "1m", timeRange.predicate());
        QueryResult result = gateway.command(sql);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("level", safeLevel);
        body.put("influxql", sql);
        body.put("rows", InfluxResults.rows(result));
        return body;
    }

    private SchemaStep createIfMissing(List<String> names, String name, String sql) {
        if (names.contains(name)) {
            return new SchemaStep(name, "skipped", "连续查询已存在");
        }
        gateway.command(sql);
        return new SchemaStep(name, "applied", sql);
    }

    private static String normalizeLevel(String level) {
        String value = InfluxQl.blankToNull(level);
        if (value == null) {
            throw new IllegalArgumentException("level 只允许 1m 或 1h");
        }
        if (!"1m".equals(value) && !"1h".equals(value)) {
            throw new IllegalArgumentException("level 只允许 1m 或 1h");
        }
        return value;
    }

    private static TimeRange closedRange(String range, String start, String end, Duration window) {
        Instant now = Instant.now();
        Instant closedEnd = now.minus(window);
        String rangeText = InfluxQl.blankToNull(range);
        String startText = InfluxQl.blankToNull(start);
        String endText = InfluxQl.blankToNull(end);
        if (rangeText != null) {
            Duration duration = InfluxQl.parseDuration(rangeText);
            if (duration.compareTo(window) <= 0) {
                throw new IllegalArgumentException("回填范围必须大于一个聚合窗口");
            }
            Instant rangeStart = closedEnd.minus(duration);
            return TimeRanges.parse(null, InfluxQl.format(rangeStart), InfluxQl.format(closedEnd), InfluxLimits.RAW_MAX_SPAN);
        }
        if (startText == null || endText == null) {
            throw new IllegalArgumentException("回填必须指定 range，或同时指定 start 与 end");
        }
        TimeRange requested = TimeRanges.parse(null, startText, endText, InfluxLimits.RAW_MAX_SPAN);
        if (Duration.between(requested.getStart(), requested.getEnd()).compareTo(window) <= 0) {
            throw new IllegalArgumentException("回填范围必须大于一个聚合窗口");
        }
        if (requested.getEnd().isAfter(closedEnd)) {
            throw new IllegalArgumentException("回填结束时间必须早于当前未完成的聚合窗口，避免和连续查询写同一段");
        }
        return requested;
    }
}
