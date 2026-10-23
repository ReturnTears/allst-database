package com.allst.dynamic.scenario.common;

import java.util.Set;

/**
 * 场景数据源键。带下划线的名字会被 dynamic-datasource 按第一个下划线分组，
 * 所以从库用 rwSlave_1 / rwSlave_2，组名是 rwSlave。其余键不要带下划线。
 */
public final class DataSourceKeys {

    public static final String LEGACY_MASTER = "master";
    public static final String LEGACY_SLAVE = "slave";

    public static final String RW_MASTER = "rwMaster";
    public static final String RW_SLAVE_GROUP = "rwSlave";
    public static final String RW_SLAVE_1 = "rwSlave_1";
    public static final String RW_SLAVE_2 = "rwSlave_2";

    public static final String ORDER_SHARD_0 = "orderShard0";
    public static final String ORDER_SHARD_1 = "orderShard1";

    public static final String TENANT_A = "tenantA";
    public static final String TENANT_B = "tenantB";

    public static final String RELEASE_STABLE = "releaseStable";
    public static final String RELEASE_GRAY = "releaseGray";

    public static final String DATA_SHARD_0 = "dataShard0";
    public static final String DATA_SHARD_1 = "dataShard1";

    private static final Set<String> PROTECTED = Set.of(
            LEGACY_MASTER, LEGACY_SLAVE,
            RW_MASTER, RW_SLAVE_1, RW_SLAVE_2,
            ORDER_SHARD_0, ORDER_SHARD_1,
            TENANT_A, TENANT_B,
            RELEASE_STABLE, RELEASE_GRAY,
            DATA_SHARD_0, DATA_SHARD_1
    );

    private DataSourceKeys() {
    }

    public static boolean protectedKey(String key) {
        return PROTECTED.contains(key);
    }
}
