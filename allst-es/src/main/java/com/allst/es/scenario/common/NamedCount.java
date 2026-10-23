package com.allst.es.scenario.common;

import lombok.Data;

/**
 * 聚合桶。count 是文档数，metric 是子聚合指标，例如金额合计或去重计数。
 */
@Data
public class NamedCount {
    private String key;
    private long count;
    private Double metric;

    public long signal() {
        if (metric != null) {
            return Math.round(metric);
        }
        return count;
    }
}
