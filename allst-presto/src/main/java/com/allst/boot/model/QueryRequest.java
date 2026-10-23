package com.allst.boot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Hutu
 * @since 2026-10-23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryRequest {

    private String sql;
    private Integer maxRows;
    private Integer timeoutSeconds;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Integer getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
