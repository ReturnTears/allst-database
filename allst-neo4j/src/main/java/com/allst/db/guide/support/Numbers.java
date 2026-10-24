package com.allst.db.guide.support;

import java.util.Map;

public final class Numbers {

    private Numbers() {
    }

    public static long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    public static long stat(Map<String, Object> stats, String key) {
        if (stats == null) {
            return 0L;
        }
        return asLong(stats.get(key));
    }
}
