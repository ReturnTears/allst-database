package com.allst.boot.cassandra.common;

import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Rows {

    private Rows() {
    }

    public static List<Map<String, Object>> list(ResultSet resultSet, int limit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Row row : resultSet) {
            rows.add(toMap(row));
            if (rows.size() >= limit) {
                break;
            }
        }
        return rows;
    }

    public static PageSlice onePage(ResultSet resultSet) {
        int available = resultSet.getAvailableWithoutFetching();
        List<Map<String, Object>> rows = new ArrayList<>(available);
        for (int i = 0; i < available; i++) {
            Row row = resultSet.one();
            if (row == null) {
                break;
            }
            rows.add(toMap(row));
        }
        String next = resultSet.isFullyFetched() ? null : PageTokens.encode(resultSet.getExecutionInfo().getPagingState());
        return new PageSlice(rows, next);
    }

    public static Map<String, Object> first(ResultSet resultSet) {
        Row row = resultSet.one();
        return row == null ? null : toMap(row);
    }

    public static Map<String, Object> toMap(Row row) {
        Map<String, Object> values = new LinkedHashMap<>();
        ColumnDefinitions definitions = row.getColumnDefinitions();
        for (int i = 0; i < definitions.size(); i++) {
            String name = definitions.get(i).getName().asInternal();
            values.put(name, row.isNull(i) ? null : row.getObject(i));
        }
        return values;
    }

    public record PageSlice(List<Map<String, Object>> rows, String pagingState) {
    }
}
