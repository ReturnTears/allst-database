package com.allst.dynamic.scenario.sharding;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderRoute {
    private String dataSource;
    private String table;
    private int dbIndex;
    private int tableIndex;
}
