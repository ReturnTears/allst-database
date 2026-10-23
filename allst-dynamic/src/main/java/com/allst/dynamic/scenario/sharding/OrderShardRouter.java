package com.allst.dynamic.scenario.sharding;

import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.ScenarioException;

/**
 * 2 库 x 2 表。userId 的低位决定库，高一位决定表，四个物理表都会被用到。
 * userId 0 -> orderShard0 / t_order_0
 * userId 1 -> orderShard1 / t_order_0
 * userId 2 -> orderShard0 / t_order_1
 * userId 3 -> orderShard1 / t_order_1
 */
public final class OrderShardRouter {

    public static final int DATABASE_COUNT = 2;
    public static final int TABLE_COUNT = 2;

    private OrderShardRouter() {
    }

    public static OrderRoute route(long userId) {
        if (userId < 0) {
            throw new ScenarioException(400, "userId 不能为负数");
        }
        int dbIndex = (int) Math.floorMod(userId, DATABASE_COUNT);
        int tableIndex = (int) Math.floorMod(userId / DATABASE_COUNT, TABLE_COUNT);
        String dataSource = dbIndex == 0 ? DataSourceKeys.ORDER_SHARD_0 : DataSourceKeys.ORDER_SHARD_1;
        return new OrderRoute(dataSource, "t_order_" + tableIndex, dbIndex, tableIndex);
    }
}
