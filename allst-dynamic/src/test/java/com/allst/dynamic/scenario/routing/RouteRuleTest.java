package com.allst.dynamic.scenario.routing;

import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.ScenarioException;
import com.allst.dynamic.scenario.gray.GrayDecider;
import com.allst.dynamic.scenario.gray.GrayPolicy;
import com.allst.dynamic.scenario.shard.ShardDirectory;
import com.allst.dynamic.scenario.sharding.OrderRoute;
import com.allst.dynamic.scenario.sharding.OrderShardRouter;
import com.allst.dynamic.scenario.tenant.TenantKeys;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteRuleTest {

    @Test
    void orderUserFallsIntoFourPhysicalTables() {
        assertRoute(0, DataSourceKeys.ORDER_SHARD_0, "t_order_0");
        assertRoute(1, DataSourceKeys.ORDER_SHARD_1, "t_order_0");
        assertRoute(2, DataSourceKeys.ORDER_SHARD_0, "t_order_1");
        assertRoute(3, DataSourceKeys.ORDER_SHARD_1, "t_order_1");
        assertRoute(4, DataSourceKeys.ORDER_SHARD_0, "t_order_0");
    }

    @Test
    void negativeUserIsRejected() {
        assertThrows(ScenarioException.class, () -> OrderShardRouter.route(-1));
    }

    @Test
    void grayHeaderOverridesAllowList() {
        GrayPolicy policy = new GrayPolicy();
        policy.replace(100, List.of("u1"));
        assertEquals(DataSourceKeys.RELEASE_GRAY, GrayDecider.decide(policy, "gray", null));
        assertEquals(DataSourceKeys.RELEASE_STABLE, GrayDecider.decide(policy, "stable", "u1"));
        assertEquals(DataSourceKeys.RELEASE_GRAY, GrayDecider.decide(policy, null, "u1"));
        assertEquals(DataSourceKeys.RELEASE_STABLE, GrayDecider.decide(policy, null, null));
    }

    @Test
    void grayPercentKeepsTheSameUserOnOneSide() {
        GrayPolicy policy = new GrayPolicy();
        policy.replace(30, List.of());
        String first = GrayDecider.decide(policy, null, "user-stable-bucket");
        assertEquals(first, GrayDecider.decide(policy, null, "user-stable-bucket"));
        policy.replace(100, List.of());
        assertEquals(DataSourceKeys.RELEASE_GRAY, GrayDecider.decide(policy, null, "anyone"));
        policy.replace(0, List.of());
        assertEquals(DataSourceKeys.RELEASE_STABLE, GrayDecider.decide(policy, null, "anyone"));
    }

    @Test
    void shardSlotStaysStableUntilMoved() {
        ShardDirectory directory = new ShardDirectory();
        ShardDirectory.ShardLocation first = directory.locate("device-7");
        assertEquals(first, directory.locate("device-7"));
        directory.move(DataSourceKeys.DATA_SHARD_0, List.of(first.getSlot()));
        assertEquals(DataSourceKeys.DATA_SHARD_0, directory.dataSourceOf("device-7"));
        assertThrows(ScenarioException.class, () -> directory.move(DataSourceKeys.DATA_SHARD_1, List.of(99)));
    }

    @Test
    void tenantKeyAvoidsUnderscore() {
        assertEquals(DataSourceKeys.TENANT_A, TenantKeys.builtinKey("a"));
        assertEquals(DataSourceKeys.TENANT_B, TenantKeys.builtinKey("b"));
        assertEquals("rtTenantc", TenantKeys.runtimeKey("c"));
        assertTrue(TenantKeys.builtin("a"));
        assertThrows(ScenarioException.class, () -> TenantKeys.checkTenantId("C_1"));
    }

    private void assertRoute(long userId, String dataSource, String table) {
        OrderRoute route = OrderShardRouter.route(userId);
        assertEquals(dataSource, route.getDataSource());
        assertEquals(table, route.getTable());
    }
}
