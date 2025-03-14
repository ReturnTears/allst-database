package com.allst.es.func;

import java.util.Arrays;

@FunctionalInterface
public interface NumFuncInterface {
    Object calc(Integer... params);

    /**
     * 求和
     */
    default Integer sum(Integer... params) {
        return Arrays.stream(params).mapToInt(Integer::intValue).sum();
    }

    /**
     * 求积
     */
    default Integer multiply(Integer... params) {
        return Arrays.stream(params).anyMatch(n -> n == 0) ? 0 : Arrays.stream(params).reduce(1, (a, b) -> a * b);
    }
}
