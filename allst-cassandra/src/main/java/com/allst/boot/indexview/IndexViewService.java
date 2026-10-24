package com.allst.boot.indexview;

import com.allst.boot.cassandra.client.CqlGateway;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.common.Limits;
import com.allst.boot.cassandra.common.Rows;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.allst.boot.cassandra.cql.Statements;
import com.allst.boot.reading.ReadingRules;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IndexViewService {

    private final CqlGateway gateway;
    private final CassandraProperties properties;

    public IndexViewService(CqlGateway gateway, CassandraProperties properties) {
        this.gateway = gateway;
        this.properties = properties;
    }

    public Map<String, Object> definitions() {
        String keyspace = properties.getKeyspace();
        List<Map<String, Object>> indexes = Rows.list(gateway.query(Statements.selectIndexes(),
                builder -> builder.setString("keyspace_name", keyspace)), 50);
        List<Map<String, Object>> views = Rows.list(gateway.query(Statements.selectViews(),
                builder -> builder.setString("keyspace_name", keyspace)), 50);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keyspace", keyspace);
        body.put("indexes", indexes);
        body.put("views", views);
        body.put("notes", List.of(
                "sensor_reading_status_idx 是 Cassandra 4.1 的普通二级索引。查询仍然要带 device_id 和 day_bucket。",
                "status 基数很低，索引切片可能很大。这里只演示等值过滤，不拿它做主查询路径。",
                "Cassandra 5.0 才把 Storage-Attached Index 作为推荐的二级索引。4.1 不要写 USING 'StorageAttachedIndex'。",
                "SASI 还是实验特性，这个项目不创建它。",
                "reading_by_metric 是物化视图。基表每次写入都会多写一份，视图和基表是最终一致。",
                "生产上如果要完全掌握写入失败，优先用 reading_by_region 这种应用双写，而不是物化视图。"
        ));
        return body;
    }

    public Map<String, Object> byStatus(String deviceId, String dayText, Integer status, Integer limit) {
        if (status == null || status < 0 || status > 9) {
            throw new IllegalArgumentException("status 必须在 0 到 9 之间");
        }
        String device = Identifiers.requireId(deviceId, "deviceId");
        LocalDate day = ReadingRules.day(dayText);
        int rowLimit = Limits.limit(limit, Limits.QUERY_DEFAULT, Limits.QUERY_MAX, "limit");
        List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.selectByStatus(properties.getKeyspace()),
                builder -> builder.setString("device_id", device).setLocalDate("day_bucket", day)
                        .setInt("status", status).setInt("row_limit", rowLimit)), rowLimit);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".sensor_reading");
        body.put("index", "sensor_reading_status_idx");
        body.put("rows", rows);
        body.put("count", rows.size());
        body.put("hint", "没有索引时 Cassandra 会拒绝这条查询。这里故意不加 ALLOW FILTERING。");
        return body;
    }

    public Map<String, Object> byMetric(String metricType, String dayText, Integer limit) {
        String metric = ReadingRules.requireMetric(metricType);
        LocalDate day = ReadingRules.day(dayText);
        int rowLimit = Limits.limit(limit, Limits.QUERY_DEFAULT, Limits.QUERY_MAX, "limit");
        List<Map<String, Object>> rows = Rows.list(gateway.query(Statements.selectByMetricView(properties.getKeyspace()),
                builder -> builder.setString("metric_type", metric).setLocalDate("day_bucket", day).setInt("row_limit", rowLimit)),
                rowLimit);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", properties.getKeyspace() + ".reading_by_metric");
        body.put("rows", rows);
        body.put("count", rows.size());
        body.put("hint", "视图的分区键是 metric_type 加 day_bucket，所以可以不带 device_id。刚写入的行可能还没出现在视图里。");
        return body;
    }
}
