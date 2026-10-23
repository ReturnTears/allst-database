package com.allst.dynamic.scenario.tenant;

import com.allst.dynamic.scenario.common.ScenarioException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantDirectory {

    private final Map<String, String> keys = new ConcurrentHashMap<>();

    public TenantDirectory() {
        keys.put("a", TenantKeys.builtinKey("a"));
        keys.put("b", TenantKeys.builtinKey("b"));
    }

    public String requireKey(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new ScenarioException(400, "缺少请求头 X-Tenant-Id");
        }
        TenantKeys.checkTenantId(tenantId);
        String key = keys.get(tenantId);
        if (key == null) {
            throw new ScenarioException(404, "租户 " + tenantId + " 还没有数据源");
        }
        return key;
    }

    public boolean builtin(String tenantId) {
        return TenantKeys.builtin(tenantId);
    }

    public void bindRuntime(String tenantId, String dataSourceKey) {
        TenantKeys.checkTenantId(tenantId);
        if (TenantKeys.builtin(tenantId) || keys.containsKey(tenantId)) {
            throw new ScenarioException(400, "租户 " + tenantId + " 已存在");
        }
        keys.put(tenantId, dataSourceKey);
    }

    public String removeRuntime(String tenantId) {
        if (TenantKeys.builtin(tenantId)) {
            throw new ScenarioException(400, "内置租户不能摘除");
        }
        String key = keys.remove(tenantId);
        if (key == null) {
            throw new ScenarioException(404, "租户 " + tenantId + " 不存在");
        }
        return key;
    }
}
