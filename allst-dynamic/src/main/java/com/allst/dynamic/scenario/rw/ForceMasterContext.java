package com.allst.dynamic.scenario.rw;

/**
 * 同一请求里已经写过主库。后续读请求留在主库，避免读到还没复制过去的从库。
 */
public final class ForceMasterContext {

    private static final ThreadLocal<Boolean> MARK = new ThreadLocal<>();

    private ForceMasterContext() {
    }

    public static void mark() {
        MARK.set(Boolean.TRUE);
    }

    public static boolean marked() {
        return Boolean.TRUE.equals(MARK.get());
    }

    public static void clear() {
        MARK.remove();
    }
}
