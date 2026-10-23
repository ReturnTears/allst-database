package com.allst.es.scenario.common;

import lombok.Data;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

@Data
public class SearchPage<T> {
    private long total;
    private List<ScenarioHit<T>> hits;

    public static <T> SearchPage<T> of(SearchHits<T> hits) {
        SearchPage<T> page = new SearchPage<T>();
        page.setTotal(hits.getTotalHits());
        page.setHits(SearchHitMapper.map(hits));
        return page;
    }
}
