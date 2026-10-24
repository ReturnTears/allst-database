package com.allst.boot.influx.common;

import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InfluxResults {

    private InfluxResults() {
    }

    public static void requireSuccess(QueryResult result) {
        if (result == null) {
            throw new InfluxAccessException("InfluxDB 返回空结果", null, false);
        }
        if (result.getError() != null) {
            throw new InfluxAccessException(result.getError(), null, false);
        }
        if (result.getResults() == null) {
            return;
        }
        for (QueryResult.Result one : result.getResults()) {
            if (one != null && one.getError() != null) {
                throw new InfluxAccessException(one.getError(), null, false);
            }
        }
    }

    public static List<Map<String, Object>> rows(QueryResult result) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (result == null || result.getResults() == null) {
            return rows;
        }
        for (QueryResult.Result one : result.getResults()) {
            if (one == null || one.getSeries() == null) {
                continue;
            }
            for (QueryResult.Series series : one.getSeries()) {
                if (series.getValues() == null || series.getColumns() == null) {
                    continue;
                }
                List<String> columns = series.getColumns();
                for (List<Object> value : series.getValues()) {
                    if (value.size() < columns.size()) {
                        throw new InfluxAccessException("InfluxDB 返回的列和值数量不一致", null, false);
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    if (series.getName() != null) {
                        row.put("measurement", series.getName());
                    }
                    if (series.getTags() != null) {
                        row.putAll(series.getTags());
                    }
                    for (int i = 0; i < columns.size(); i++) {
                        row.put(columns.get(i), value.get(i));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public static List<String> column(QueryResult result, String name) {
        List<String> values = new ArrayList<>();
        for (Map<String, Object> row : rows(result)) {
            Object value = row.get(name);
            if (value != null) {
                values.add(String.valueOf(value));
            }
        }
        return values;
    }
}
