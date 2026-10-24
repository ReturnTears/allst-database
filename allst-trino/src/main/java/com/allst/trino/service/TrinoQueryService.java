package com.allst.trino.service;

import com.allst.trino.config.TrinoDataSourceConfig;
import com.allst.trino.config.TrinoProperties;
import com.allst.trino.exception.TrinoQueryException;
import com.allst.trino.model.QueryRequest;
import com.allst.trino.model.QueryResult;
import com.allst.trino.support.TrinoSqlGuard;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 对 Trino 的查询入口。元数据接口拼出的 SQL 由服务端固定，自定义 SQL 先经过只读检查。
 *
 * @author Hutu
 * @since 2026-10-23
 */
@Service
public class TrinoQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final TrinoProperties properties;

    public TrinoQueryService(@Qualifier(TrinoDataSourceConfig.JDBC_TEMPLATE) JdbcTemplate jdbcTemplate,
                             TrinoProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public Map<String, Object> info() {
        TrinoProperties.Jdbc jdbc = properties.getJdbc();
        TrinoProperties.Query query = properties.getQuery();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("driverClassName", jdbc.getDriverClassName());
        info.put("url", jdbc.getUrl());
        info.put("username", jdbc.getUsername());
        info.put("ssl", jdbc.isSsl());
        info.put("source", jdbc.getSource());
        info.put("readOnly", query.isReadOnly());
        info.put("maxRows", query.getMaxRows());
        info.put("timeoutSeconds", query.getTimeoutSeconds());
        return info;
    }

    public List<Map<String, String>> samples() {
        List<Map<String, String>> samples = new ArrayList<>();
        samples.add(sample("连通性", "SELECT 1 AS ok"));
        samples.add(sample("目录", "SHOW CATALOGS"));
        samples.add(sample("tpch schema", "SHOW SCHEMAS FROM tpch"));
        samples.add(sample("tpch.tiny 表", "SHOW TABLES FROM tpch.tiny"));
        samples.add(sample("国家维表",
                "SELECT nationkey, name, regionkey FROM tpch.tiny.nation ORDER BY nationkey"));
        samples.add(sample("订单状态汇总",
                "SELECT orderstatus, count(*) AS order_count FROM tpch.sf1.orders GROUP BY orderstatus ORDER BY orderstatus"));
        samples.add(sample("协调节点",
                "SELECT node_id, http_uri, node_version, coordinator, state FROM system.runtime.nodes"));
        return samples;
    }

    public Map<String, Object> health() {
        long start = System.currentTimeMillis();
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT 1 AS ok");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("result", normalizeRow(row));
        body.put("elapsedMs", System.currentTimeMillis() - start);
        return body;
    }

    public QueryResult nodes() {
        return execute(
                "SELECT node_id, http_uri, node_version, coordinator, state FROM system.runtime.nodes",
                null,
                null);
    }

    public List<String> catalogs() {
        return firstColumn("SHOW CATALOGS");
    }

    public List<String> schemas(String catalog) {
        String safeCatalog = TrinoSqlGuard.requireIdentifier(catalog, "catalog");
        String sql = "SELECT schema_name FROM " + safeCatalog + ".information_schema.schemata ORDER BY schema_name";
        return firstColumn(sql);
    }

    public List<String> tables(String catalog, String schema) {
        String safeCatalog = TrinoSqlGuard.requireIdentifier(catalog, "catalog");
        String safeSchema = TrinoSqlGuard.requireIdentifier(schema, "schema");
        String sql = "SELECT table_name FROM " + safeCatalog
                + ".information_schema.tables WHERE table_schema = ? ORDER BY table_name";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), safeSchema);
    }

    public QueryResult query(QueryRequest request) {
        if (request == null) {
            throw new TrinoQueryException("请求体不能为空");
        }
        return execute(request.getSql(), request.getMaxRows(), request.getTimeoutSeconds());
    }

    private List<String> firstColumn(String sql) {
        QueryResult result = execute(sql, null, null);
        List<String> values = new ArrayList<>();
        for (Map<String, Object> row : result.getRows()) {
            if (!row.isEmpty()) {
                Object value = row.values().iterator().next();
                values.add(value == null ? null : String.valueOf(value));
            }
        }
        return values;
    }

    private QueryResult execute(String sql, Integer requestedMaxRows, Integer requestedTimeout) {
        String checkedSql = TrinoSqlGuard.check(sql, properties.getQuery().isReadOnly());
        int maxRows = resolveMaxRows(requestedMaxRows);
        int timeoutSeconds = resolveTimeout(requestedTimeout);
        long start = System.currentTimeMillis();
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(checkedSql);
            statement.setMaxRows(maxRows + 1);
            statement.setQueryTimeout(timeoutSeconds);
            return statement;
        }, resultSet -> {
            return readResult(checkedSql, resultSet, maxRows, start);
        });
    }

    private QueryResult readResult(String sql, ResultSet resultSet, int maxRows, long start) throws SQLException {
        List<String> columns = columnLabels(resultSet.getMetaData());
        List<Map<String, Object>> rows = new ArrayList<>();
        boolean truncated = false;
        while (resultSet.next()) {
            if (rows.size() >= maxRows) {
                truncated = true;
                break;
            }
            rows.add(readRow(resultSet, columns));
        }
        return new QueryResult(sql, columns, rows, truncated, System.currentTimeMillis() - start);
    }

    private List<String> columnLabels(ResultSetMetaData metaData) throws SQLException {
        List<String> columns = new ArrayList<>();
        Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String label = metaData.getColumnLabel(i);
            int count = seen.merge(label, 1, Integer::sum);
            columns.add(count == 1 ? label : label + "_" + count);
        }
        return columns;
    }

    private Map<String, Object> readRow(ResultSet resultSet, List<String> columns) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), normalize(resultSet.getObject(i + 1)));
        }
        return row;
    }

    private Map<String, Object> normalizeRow(Map<String, Object> row) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        row.forEach((key, value) -> normalized.put(key, normalize(value)));
        return normalized;
    }

    private Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) value);
        }
        if (value instanceof java.sql.Date || value instanceof java.sql.Time || value instanceof java.sql.Timestamp
                || value instanceof java.time.temporal.TemporalAccessor) {
            return value.toString();
        }
        return value;
    }

    private int resolveMaxRows(Integer requested) {
        int ceiling = properties.getQuery().getMaxRows();
        if (requested == null) {
            return ceiling;
        }
        if (requested < 1) {
            throw new TrinoQueryException("maxRows 必须大于 0");
        }
        return Math.min(requested, ceiling);
    }

    private int resolveTimeout(Integer requested) {
        int ceiling = properties.getQuery().getTimeoutSeconds();
        if (requested == null) {
            return ceiling;
        }
        if (requested < 1) {
            throw new TrinoQueryException("timeoutSeconds 必须大于 0");
        }
        return Math.min(requested, ceiling);
    }

    private Map<String, String> sample(String name, String sql) {
        Map<String, String> sample = new LinkedHashMap<>();
        sample.put("name", name);
        sample.put("sql", sql);
        return sample;
    }
}
