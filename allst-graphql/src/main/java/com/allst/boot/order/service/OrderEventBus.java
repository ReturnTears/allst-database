package com.allst.boot.order.service;

import com.allst.boot.order.dto.OrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class OrderEventBus {

    private static final Logger log = LoggerFactory.getLogger(OrderEventBus.class);

    private final Sinks.Many<OrderView> sink = Sinks.many().multicast().directBestEffort();

    public void publish(OrderView order) {
        Sinks.EmitResult result = sink.tryEmitNext(order);
        if (result == Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
            log.debug("没有订阅者，忽略订单事件 {}", order.getOrderNo());
            return;
        }
        if (result.isFailure()) {
            log.warn("订单状态事件未发出: {}", result);
        }
    }

    public Flux<OrderView> stream() {
        return sink.asFlux();
    }
}
