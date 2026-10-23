package com.allst.boot.guide.service;

import com.allst.boot.guide.entity.CustomerOrder;
import com.allst.boot.guide.repository.CustomerOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final CustomerOrderRepository customerOrderRepository;

    public OrderQueryService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    /**
     * 先查出订单，再逐个访问 items。@BatchSize 会把加载收成若干条 IN 查询，仍然多于 1 条。
     */
    public int countItemsNaive() {
        int total = 0;
        for (CustomerOrder order : customerOrderRepository.findAll()) {
            total += order.getItems().size();
        }
        return total;
    }

    public int countItemsFetchJoin() {
        int total = 0;
        for (CustomerOrder order : customerOrderRepository.findAllWithItems()) {
            total += order.getItems().size();
        }
        return total;
    }

    public int countItemsByGraph() {
        int total = 0;
        for (CustomerOrder order : customerOrderRepository.findAllWithGraph()) {
            total += order.getItems().size();
        }
        return total;
    }

    /**
     * 分页不能和 join fetch 写在同一条查询里。先分页拿主键，再按主键抓取集合。
     */
    public List<CustomerOrder> pageWithItems(Pageable pageable) {
        Page<Long> ids = customerOrderRepository.pageIds(pageable);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return customerOrderRepository.findWithItemsByIdIn(ids.getContent());
    }
}
