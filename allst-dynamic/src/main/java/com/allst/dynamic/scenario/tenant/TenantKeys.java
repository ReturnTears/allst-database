package com.allst.dynamic.scenario.tenant;

import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.ScenarioException;

import java.util.regex.Pattern;

/**
 * 内置租户 a、b 对应配置文件里的数据源。运行时注册的租户使用 rtTenant 前缀，避免名字里出现下划线被当成从库分组。
 */
public final class TenantKeys {

    public static final Pattern TENANT_ID = Pattern.compile("^[a-z][a-z0-9]{0,15}$");

    private TenantKeys() {
    }

    public static void checkTenantId(String tenantId) {
        if (tenantId == null || !TENANT_ID.matcher(tenantId).matches()) {
            throw new ScenarioException(400, "tenantId 只能是小写字母开头，后接小写字母或数字，最长 16 位");
        }
    }

    public static String builtinKey(String tenantId) {
        if ("a".equals(tenantId)) {
            return DataSourceKeys.TENANT_A;
        }
        if ("b".equals(tenantId)) {
            return DataSourceKeys.TENANT_B;
        }
        return null;
    }

    public static boolean builtin(String tenantId) {
        return builtinKey(tenantId) != null;
    }

    public static String runtimeKey(String tenantId) {
        checkTenantId(tenantId);
        return "rtTenant" + tenantId;
    }
}
