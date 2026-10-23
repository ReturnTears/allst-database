package com.allst.es.scenario.common;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.ArrayList;
import java.util.List;

public final class SearchHitMapper {
    private SearchHitMapper() {
    }

    public static <T> List<ScenarioHit<T>> map(SearchHits<T> hits) {
        List<ScenarioHit<T>> result = new ArrayList<ScenarioHit<T>>();
        for (SearchHit<T> hit : hits.getSearchHits()) {
            result.add(one(hit, null));
        }
        return result;
    }

    public static <T> ScenarioHit<T> one(SearchHit<T> hit, Double distanceKm) {
        float score = hit.getScore();
        if (Float.isNaN(score) || Float.isInfinite(score)) {
            score = 0F;
        }
        return ScenarioHit.<T>builder()
                .id(hit.getId())
                .score(score)
                .distanceKm(distanceKm)
                .source(hit.getContent())
                .highlight(hit.getHighlightFields())
                .build();
    }

    public static Double distanceOf(SearchHit<?> hit) {
        if (hit.getSortValues() == null || hit.getSortValues().isEmpty()) {
            return null;
        }
        Object value = hit.getSortValues().get(0);
        if (!(value instanceof Number)) {
            return null;
        }
        double distance = ((Number) value).doubleValue();
        if (Double.isNaN(distance) || Double.isInfinite(distance)) {
            return null;
        }
        return distance;
    }
}
