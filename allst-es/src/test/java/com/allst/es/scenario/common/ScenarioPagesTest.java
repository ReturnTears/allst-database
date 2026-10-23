package com.allst.es.scenario.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScenarioPagesTest {

    @Test
    void usesDefaultsAndCapsPageSize() {
        Pageable defaults = ScenarioPages.of(null, null);
        assertEquals(0, defaults.getPageNumber());
        assertEquals(10, defaults.getPageSize());

        Pageable capped = ScenarioPages.of(-1, 500);
        assertEquals(0, capped.getPageNumber());
        assertEquals(50, capped.getPageSize());
    }
}
