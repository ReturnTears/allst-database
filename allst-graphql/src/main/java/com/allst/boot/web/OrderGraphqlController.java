package com.allst.boot.web;

import com.allst.boot.catalog.dto.ProductView;
import com.allst.boot.catalog.service.CatalogService;
import com.allst.boot.common.PageInput;
import com.allst.boot.common.Slice;
import com.allst.boot.customer.dto.CustomerView;
import com.allst.boot.customer.service.CustomerService;
import com.allst.boot.order.dto.CreateOrderInput;
import com.allst.boot.order.dto.OrderItemView;
import com.allst.boot.order.dto.OrderView;
import com.allst.boot.order.enums.OrderStatus;
import com.allst.boot.order.service.OrderEventBus;
import com.allst.boot.order.service.OrderService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class OrderGraphqlController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final CatalogService catalogService;
    private final OrderEventBus eventBus;

    public OrderGraphqlController(OrderService orderService,
                                  CustomerService customerService,
                                  CatalogService catalogService,
                                  OrderEventBus eventBus) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.catalogService = catalogService;
        this.eventBus = eventBus;
    }

    @QueryMapping
    public OrderView order(@Argument Long id) {
        return orderService.get(id);
    }

    @QueryMapping
    public OrderView orderByNo(@Argument String orderNo) {
        return orderService.getByNo(orderNo);
    }

    @QueryMapping
    public Slice<OrderView> orders(@Argument Long customerId, @Argument OrderStatus status, @Argument PageInput page) {
        return orderService.page(customerId, status, page);
    }

    @MutationMapping
    public OrderView createOrder(@Argument CreateOrderInput input) {
        return orderService.create(input);
    }

    @MutationMapping
    public OrderView updateOrderStatus(@Argument Long id, @Argument OrderStatus status) {
        return orderService.updateStatus(id, status);
    }

    @MutationMapping
    public OrderView cancelOrder(@Argument Long id) {
        return orderService.cancel(id);
    }

    @MutationMapping
    public boolean deleteOrder(@Argument Long id) {
        return orderService.delete(id);
    }

    @SubscriptionMapping
    public Flux<OrderView> orderStatusChanged(@Argument Long orderId) {
        return eventBus.stream().filter(order -> orderId == null || orderId.equals(order.getId()));
    }

    @BatchMapping(typeName = "Order")
    public Map<OrderView, CustomerView> customer(List<OrderView> orders) {
        Set<Long> ids = new HashSet<Long>();
        for (OrderView order : orders) {
            ids.add(order.getCustomerId());
        }
        Map<Long, CustomerView> byId = customerService.customersByIds(ids);
        Map<OrderView, CustomerView> result = new HashMap<OrderView, CustomerView>();
        for (OrderView order : orders) {
            result.put(order, byId.get(order.getCustomerId()));
        }
        return result;
    }

    @BatchMapping(typeName = "Customer", field = "orders")
    public Map<CustomerView, List<OrderView>> customerOrders(List<CustomerView> customers) {
        return orderService.ordersOf(customers);
    }

    @BatchMapping(typeName = "OrderItem")
    public Map<OrderItemView, ProductView> product(List<OrderItemView> items) {
        return catalogService.productsOf(items);
    }
}
