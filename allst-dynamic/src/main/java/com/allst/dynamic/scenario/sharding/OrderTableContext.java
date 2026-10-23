package com.allst.dynamic.scenario.sharding;

/**
 * MyBatis-Plus 改写表名时读取。只在订单场景的调用栈里赋值，用完必须清掉。
 */
public final class OrderTableContext {

    private static final ThreadLocal<String> TABLE = new ThreadLocal<>();

    private OrderTableContext() {
    }

    public static void set(String table) {
        TABLE.set(table);
    }

    public static String get() {
        return TABLE.get();
    }

    public static void clear() {
        TABLE.remove();
    }
}
