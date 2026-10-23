package com.allst.es.scenario.common;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.time.LocalDateTime;

public final class EsQuerySupport {
    private EsQuerySupport() {
    }

    public static void filterTime(BoolQueryBuilder bool, String field, LocalDateTime start, LocalDateTime end) {
        bool.filter(QueryBuilders.rangeQuery(field)
                .gte(ScenarioTimes.format(start))
                .lte(ScenarioTimes.format(end)));
    }
}
