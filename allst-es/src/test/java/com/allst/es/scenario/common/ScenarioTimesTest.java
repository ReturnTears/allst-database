package com.allst.es.scenario.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScenarioTimesTest {

    @Test
    void parsesDateAndDateTime() {
        assertNull(ScenarioTimes.parse("  "));
        assertEquals(LocalDateTime.of(2026, 10, 23, 0, 0), ScenarioTimes.parse("2026-10-23"));
        assertEquals(LocalDateTime.of(2026, 10, 23, 19, 30, 0), ScenarioTimes.parse("2026-10-23 19:30:00"));
        assertEquals(LocalDateTime.of(2026, 10, 23, 19, 30, 0), ScenarioTimes.parse("2026-10-23T19:30"));
    }

    @Test
    void rejectsInvalidTextAndReversedWindow() {
        assertThrows(IllegalArgumentException.class, () -> ScenarioTimes.parse("2026/10/23"));
        assertThrows(IllegalArgumentException.class, () -> ScenarioTimes.window("2026-10-24", "2026-10-23", 1));
    }

    @Test
    void choosesHourForShortWindow() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 23, 8, 0);
        LocalDateTime end = start.plusHours(6);
        assertEquals("hour", ScenarioTimes.chooseInterval(null, start, end));
        assertEquals("day", ScenarioTimes.chooseInterval("day", start, end));
        assertEquals("day", ScenarioTimes.chooseInterval(null, start, start.plusDays(3)));
    }
}
