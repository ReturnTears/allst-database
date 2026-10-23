package com.allst.es.scenario.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class ScenarioPages {
    private ScenarioPages() {
    }

    public static Pageable of(Integer page, Integer size) {
        int pageNumber = page == null || page < 0 ? 0 : page;
        int pageSize = size == null || size < 1 ? 10 : Math.min(size, 50);
        return PageRequest.of(pageNumber, pageSize);
    }
}
