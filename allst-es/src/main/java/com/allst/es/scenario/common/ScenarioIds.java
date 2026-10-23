package com.allst.es.scenario.common;

import java.util.UUID;

public final class ScenarioIds {
    private ScenarioIds() {
    }

    public static String next() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
