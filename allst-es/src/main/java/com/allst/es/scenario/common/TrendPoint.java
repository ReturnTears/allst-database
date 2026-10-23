package com.allst.es.scenario.common;

import lombok.Data;

@Data
public class TrendPoint {
    private String time;
    private long count;
    private Double metric;
}
