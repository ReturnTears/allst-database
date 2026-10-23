package com.allst.dynamic.scenario.sharding;

import com.allst.dynamic.scenario.common.Checks;
import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.DataSources;
import com.allst.dynamic.scenario.common.RouteProbe;
import com.allst.dynamic.scenario.common.RouteView;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TradeOrderServiceImpl implements TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final RouteProbe routeProbe;

    public TradeOrderServiceImpl(TradeOrderMapper tradeOrderMapper, RouteProbe routeProbe) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.routeProbe = routeProbe;
    }

    @Override
    public OrderRoute locate(long userId) {
        return OrderShardRouter.route(Checks.requireUserId(userId));
    }

    @Override
    public RouteView<TradeOrder> create(OrderCommand command) {
        if (command == null) {
            throw new com.allst.dynamic.scenario.common.ScenarioException(400, "请求体不能为空");
        }
        long userId = Checks.requireUserId(command.getUserId());
        String orderNo = command.getOrderNo() == null || command.getOrderNo().isBlank()
                ? "ORD-" + userId + "-" + System.currentTimeMillis()
                : Checks.requireText(command.getOrderNo(), "orderNo", 64);
        OrderRoute route = OrderShardRouter.route(userId);
        TradeOrder order = new TradeOrder();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAmount(Checks.requireAmount(command.getAmount()));
        order.setCreatedAt(LocalDateTime.now());
        return execute(route, "userId % 2 选库，(userId / 2) % 2 选表", () -> {
            tradeOrderMapper.insert(order);
            return order;
        });
    }

    @Override
    public RouteView<List<TradeOrder>> listByUser(long userId) {
        long normalized = Checks.requireUserId(userId);
        OrderRoute route = OrderShardRouter.route(normalized);
        return execute(route, "带分片键查询，只打到一个库的一张表", () -> tradeOrderMapper.selectList(
                new QueryWrapper<TradeOrder>().eq("user_id", normalized).orderByDesc("id")));
    }

    @Override
    public List<RouteView<List<TradeOrder>>> scatter() {
        List<RouteView<List<TradeOrder>>> all = new ArrayList<>();
        for (String dataSource : List.of(DataSourceKeys.ORDER_SHARD_0, DataSourceKeys.ORDER_SHARD_1)) {
            for (String table : List.of("t_order_0", "t_order_1")) {
                OrderRoute route = new OrderRoute(dataSource, table, dataSource.endsWith("0") ? 0 : 1,
                        table.endsWith("0") ? 0 : 1);
                all.add(execute(route, "管理端散查。线上按用户查询必须带 userId",
                        () -> tradeOrderMapper.selectList(new QueryWrapper<TradeOrder>().orderByAsc("id"))));
            }
        }
        return all;
    }

    private <T> RouteView<T> execute(OrderRoute route, String reason, java.util.function.Supplier<T> action) {
        return DataSources.call(route.getDataSource(), () -> {
            OrderTableContext.set(route.getTable());
            try {
                T records = action.get();
                return RouteView.of(route.getDataSource(), routeProbe.database(), route.getTable(), reason, records);
            } finally {
                OrderTableContext.clear();
            }
        });
    }
}
