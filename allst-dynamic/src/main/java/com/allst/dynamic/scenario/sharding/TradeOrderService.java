package com.allst.dynamic.scenario.sharding;

import com.allst.dynamic.scenario.common.RouteView;

import java.util.List;

public interface TradeOrderService {

    OrderRoute locate(long userId);

    RouteView<TradeOrder> create(OrderCommand command);

    RouteView<List<TradeOrder>> listByUser(long userId);

    List<RouteView<List<TradeOrder>>> scatter();
}
