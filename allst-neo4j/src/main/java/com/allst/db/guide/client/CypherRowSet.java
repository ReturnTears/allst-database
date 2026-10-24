package com.allst.db.guide.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CypherRowSet {

    private final List<String> columns;
    private final List<Map<String, Object>> rows;
    private final Map<String, Object> stats;
    private final Map<String, Object> plan;
    private final long elapsedMs;

    public CypherRowSet(List<String> columns, List<Map<String, Object>> rows,
                        Map<String, Object> stats, Map<String, Object> plan, long elapsedMs) {
        this.columns = columns == null ? Collections.emptyList() : columns;
        this.rows = rows == null ? Collections.emptyList() : rows;
        this.stats = stats == null ? Collections.emptyMap() : stats;
        this.plan = plan;
        this.elapsedMs = elapsedMs;
    }

    public static CypherRowSet empty() {
        return new CypherRowSet(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), null, 0L);
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public Map<String, Object> getPlan() {
        return plan;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }
}
