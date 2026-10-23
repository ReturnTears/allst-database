package com.allst.dynamic.scenario.common;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;

import java.util.function.Supplier;

/**
 * 在当前线程压入数据源键，执行完后弹出。和 @DS 一样使用栈，可以嵌套。
 */
public final class DataSources {

    private DataSources() {
    }

    public static <T> T call(String key, Supplier<T> action) {
        DynamicDataSourceContextHolder.push(key);
        try {
            return action.get();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    public static void run(String key, Runnable action) {
        call(key, () -> {
            action.run();
            return null;
        });
    }
}
