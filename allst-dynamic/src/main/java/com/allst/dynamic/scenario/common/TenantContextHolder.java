package com.allst.dynamic.scenario.common;

/**
 * 当前请求的租户标识。由过滤器写入，业务代码只读。
 */
public final class TenantContextHolder {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static String get() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
