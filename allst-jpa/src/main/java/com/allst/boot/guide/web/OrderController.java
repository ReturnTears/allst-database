package com.allst.boot.guide.web;

import com.allst.boot.guide.service.OrderQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guide/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;

    public OrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/item-count")
    public int itemCount() {
        return orderQueryService.countItemsFetchJoin();
    }

    @GetMapping("/page-size")
    public int pageSize(@PageableDefault(size = 5) Pageable pageable) {
        return orderQueryService.pageWithItems(pageable).size();
    }
}
