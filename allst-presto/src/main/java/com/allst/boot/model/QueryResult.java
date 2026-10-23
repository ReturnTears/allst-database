package com.allst.boot.model;

import java.util.List;
import java.util.Map;

/**
 * @author Hutu
 * @since 2026-10-23
 */
public class QueryResult {

    private final String sql;
    private final List<String> columns;
    private final List<Map<String, Object>> rows;
    private final int rowCount;
    private final boolean truncated;
    private final long elapsedMs;

    public QueryResult(String sql, List<String> columns, List<Map<String, Object>> rows,
                       boolean truncated, long elapsedMs) {
        this.sql = sql;
        this.columns = columns;
        this.rows = rows;
        this.rowCount = rows.size();
        this.truncated = truncated;
        this.elapsedMs = elapsedMs;
    }

    public String getSql() {
        return sql;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }
}
