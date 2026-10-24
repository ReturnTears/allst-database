package com.allst.boot.continuous;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.cassandra.common.PageTokens;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.allst.boot.reading.ReadingRules;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContinuousService {

    private final CqlGateway gateway;
    private final CassandraProperties properties;
    private final ZoneId zone;

    public ContinuousService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
        this.zone = ZoneId.of(properties.getZone());
    }

    public Map<String, Object> page(String deviceId, String dayText, String metricType, Integer pageSize, String pagingState) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        String metric = ReadingRules.optionalMetric(metricType);
        int size = Limits.limit(pageSize, Limits.PAGE_DEFAULT, Limits.PAGE_MAX, "pageSize");
        ByteBuffer state = PageTokens.decode(pagingState);
        Rows.PageSlice page = Rows.onePage(gateway.query(Statements.pageByDevice(properties.getKeyspace(), metric != null),
                builder -> bindPage(builder, device, day, metric, size, state)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("rows", page.rows());
        body.put("count", page.rows().size());
        body.put("pageSize", size);
        body.put("pagingState", page.pagingState());
        body.put("hint", "把 pagingState 原样传回来取下一页。它只对这一条查询有效，换了设备或日期就要丢掉。");
        return body;
    }

    public Map<String, Object> scan(Integer pageSize, String pagingState, String confirm) {
        if (!"scan".equals(confirm)) {
            throw new IllegalArgumentException("全表连续扫描必须带 confirm=scan");
        }
        int size = Limits.limit(pageSize, Limits.PAGE_DEFAULT, Limits.PAGE_MAX, "pageSize");
        ByteBuffer state = PageTokens.decode(pagingState);
        Rows.PageSlice page = Rows.onePage(gateway.query(Statements.scan(properties.getKeyspace()), builder -> {
            builder.setPageSize(size);
            if (state != null) {
                builder.setPagingState(state);
            }
        }));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("rows", page.rows());
        body.put("count", page.rows().size());
        body.put("pageSize", size);
        body.put("pagingState", page.pagingState());
        body.put("hint", "这是按 token 环连续翻页，用来导出或回填。在线接口不要对用户的一次请求做全表扫描。");
        return body;
    }

    public Map<String, Object> rollup(String deviceId, String dayText) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        ReadPage read = readPartition(device, day);
        Map<HourlyRollup.Key, HourlyRollup.Aggregate> buckets = HourlyRollup.accumulate(read.points(), zone);
        for (Map.Entry<HourlyRollup.Key, HourlyRollup.Aggregate> entry : buckets.entrySet()) {
            HourlyRollup.Aggregate aggregate = entry.getValue();
            gateway.execute(gateway.mutation(Statements.insertHourly(properties.getKeyspace()), true)
                    .setString("device_id", device)
                    .setLocalDate("day_bucket", day)
                    .setInstant("hour_bucket", entry.getKey().hourBucket())
                    .setString("metric_type", entry.getKey().metricType())
                    .setInt("sample_count", aggregate.count())
                    .setDouble("value_sum", aggregate.sum())
                    .setDouble("value_min", aggregate.min())
                    .setDouble("value_max", aggregate.max())
                    .build());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_hourly");
        body.put("rowsRead", read.points().size());
        body.put("buckets", buckets.size());
        body.put("truncated", read.truncated());
        body.put("hint", "同一天重复执行会覆盖小时表。CQL 不能按 date_trunc 分组，所以小时桶在应用里切。");
        return body;
    }

    public Map<String, Object> hourly(String deviceId, String dayText, Integer limit) {
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        int rowLimit = Limits.limit(limit, Limits.QUERY_DEFAULT, Limits.QUERY_MAX, "limit");
        List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.selectHourly(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day).setInt("row_limit", rowLimit)),
                rowLimit);
        for (Map<String, Object> row : rows) {
            Object count = row.get("sample_count");
            Object sum = row.get("value_sum");
            if (count instanceof Number countNumber && sum instanceof Number sumNumber && countNumber.doubleValue() > 0d) {
                row.put("value_avg", sumNumber.doubleValue() / countNumber.doubleValue());
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_hourly");
        body.put("rows", rows);
        body.put("count", rows.size());
        body.put("hint", "value_avg 由 sum 和 count 现算，不入库，重复降采样不会把平均值再平均一次。");
        return body;
    }

    private ReadPage readPartition(String device, LocalDate day) {
        List<HourlyRollup.Point> points = new ArrayList<>();
        ByteBuffer state = null;
        String previous = null;
        boolean truncated = false;
        while (points.size() < Limits.ROLLUP_MAX_ROWS) {
            ByteBuffer paging = state;
            Rows.PageSlice page = Rows.onePage(gateway.query(Statements.pageByDevice(properties.getKeyspace(), false),
                    builder -> bindPage(builder, device, day, null, Limits.PAGE_MAX, paging)));
            for (Map<String, Object> row : page.rows()) {
                points.add(point(row));
                if (points.size() >= Limits.ROLLUP_MAX_ROWS) {
                    truncated = true;
                    break;
                }
            }
            if (truncated || page.rows().isEmpty() || page.pagingState() == null || page.pagingState().equals(previous)) {
                break;
            }
            previous = page.pagingState();
            state = PageTokens.decode(page.pagingState());
        }
        return new ReadPage(points, truncated);
    }

    private static void bindPage(BoundStatementBuilder builder, String device, LocalDate day, String metric, int pageSize,
                                 ByteBuffer pagingState) {
        builder.setString("device_id", device).setLocalDate("day_bucket", day).setPageSize(pageSize);
        if (metric != null) {
            builder.setString("metric_type", metric);
        }
        if (pagingState != null) {
            builder.setPagingState(pagingState);
        }
    }

    private static HourlyRollup.Point point(Map<String, Object> row) {
        Object metric = row.get("metric_type");
        Object time = row.get("recorded_at");
        Object value = row.get("sensor_value");
        if (!(metric instanceof String metricType) || !(time instanceof Instant recordedAt) || !(value instanceof Number number)) {
            throw new IllegalArgumentException("降采样读到了无法识别的列");
        }
        return new HourlyRollup.Point(metricType, recordedAt, number.doubleValue());
    }

    private record ReadPage(List<HourlyRollup.Point> points, boolean truncated) {
    }
}
