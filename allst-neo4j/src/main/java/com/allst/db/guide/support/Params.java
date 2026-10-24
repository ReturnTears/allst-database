package com.allst.db.guide.support;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Java 11 没有可放入 null 的 Map.of，这里用成对参数组装查询参数。
 */
public final class Params {

    private Params() {
    }

    public static Map<String, Object> of(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("参数必须成对出现");
        }
        Map<String, Object> params = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            params.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return params;
    }
}
