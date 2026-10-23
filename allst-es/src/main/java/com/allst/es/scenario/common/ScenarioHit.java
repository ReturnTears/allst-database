package com.allst.es.scenario.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 一条检索命中。highlight 只在做了高亮的查询里有值，distanceKm 只在地理距离排序时有值。
 */
@Data
@Builder
public class ScenarioHit<T> {
    private String id;
    private float score;
    private Double distanceKm;
    private T source;
    private Map<String, List<String>> highlight;
}
