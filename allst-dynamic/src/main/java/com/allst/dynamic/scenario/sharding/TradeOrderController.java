package com.allst.dynamic.scenario.sharding;

import com.allst.dynamic.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/sharding")
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    public TradeOrderController(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }

    @GetMapping("/route")
    public ScenarioResponse<OrderRoute> route(@RequestParam("userId") long userId) {
        return ScenarioResponse.ok(tradeOrderService.locate(userId));
    }

    @PostMapping("/orders")
    public ScenarioResponse<?> create(@RequestBody OrderCommand command) {
        return ScenarioResponse.ok(tradeOrderService.create(command));
    }

    @GetMapping("/orders")
    public ScenarioResponse<?> list(@RequestParam("userId") long userId) {
        return ScenarioResponse.ok(tradeOrderService.listByUser(userId));
    }

    @GetMapping("/orders/scatter")
    public ScenarioResponse<?> scatter() {
        return ScenarioResponse.ok(tradeOrderService.scatter());
    }
}
