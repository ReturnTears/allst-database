package com.allst.es.scenario.siem;

import com.allst.es.scenario.common.NamedCount;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiemRuleEngineTest {
    private final SiemRuleEngine engine = new SiemRuleEngine();

    @Test
    void raisesAlertWhenCountReachesThreshold() {
        List<SecurityAlert> alerts = engine.evaluate(
                Arrays.asList(count("198.51.100.23", 6), count("198.51.100.40", 1)),
                5,
                Arrays.asList(metric("203.0.113.10", 8, 8)),
                6,
                Arrays.asList(count("alice", 3), count("carol", 1)),
                3);

        assertEquals(3, alerts.size());
        assertEquals("BRUTE_FORCE", alerts.get(0).getRuleCode());
        assertEquals("198.51.100.23", alerts.get(0).getSubject());
        assertEquals("PORT_SCAN", alerts.get(1).getRuleCode());
        assertEquals(8L, alerts.get(1).getCount());
        assertEquals("PRIVILEGE_CHANGE", alerts.get(2).getRuleCode());
        assertEquals("alice", alerts.get(2).getSubject());
    }

    @Test
    void staysQuietBelowThreshold() {
        List<SecurityAlert> alerts = engine.evaluate(
                Collections.singletonList(count("198.51.100.23", 4)),
                5,
                Collections.singletonList(metric("203.0.113.10", 8, 2)),
                6,
                Collections.singletonList(count("alice", 2)),
                3);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void usesDefaultThresholdWhenCallerPassesZero() {
        List<SecurityAlert> alerts = engine.evaluate(
                Collections.singletonList(count("198.51.100.23", 5)),
                0,
                Collections.<NamedCount>emptyList(),
                0,
                Collections.<NamedCount>emptyList(),
                0);
        assertEquals(1, alerts.size());
        assertEquals(5, alerts.get(0).getThreshold());
    }

    private NamedCount count(String key, long count) {
        NamedCount item = new NamedCount();
        item.setKey(key);
        item.setCount(count);
        return item;
    }

    private NamedCount metric(String key, long count, double metric) {
        NamedCount item = count(key, count);
        item.setMetric(metric);
        return item;
    }
}
