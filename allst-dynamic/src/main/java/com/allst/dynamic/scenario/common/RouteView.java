package com.allst.dynamic.scenario.common;

import lombok.Data;

/**
 * 一次路由后的结果。dataSource 是逻辑键，database 是连接上 SELECT DATABASE() 的返回值。
 */
@Data
public class RouteView<T> {
    private String dataSource;
    private String database;
    private String table;
    private String reason;
    private T records;

    public static <T> RouteView<T> of(String dataSource, String database, String table, String reason, T records) {
        RouteView<T> view = new RouteView<>();
        view.setDataSource(dataSource);
        view.setDatabase(database);
        view.setTable(table);
        view.setReason(reason);
        view.setRecords(records);
        return view;
    }
}
