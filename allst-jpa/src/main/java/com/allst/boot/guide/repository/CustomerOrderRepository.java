package com.allst.boot.guide.repository;

import com.allst.boot.guide.entity.CustomerOrder;
import com.allst.boot.guide.repository.support.GuideBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CustomerOrderRepository extends GuideBaseRepository<CustomerOrder, Long> {

    @Query("select o.id from CustomerOrder o")
    Page<Long> pageIds(Pageable pageable);

    @Query("select distinct o from CustomerOrder o left join fetch o.items where o.id in :ids")
    List<CustomerOrder> findWithItemsByIdIn(@Param("ids") Collection<Long> ids);

    @EntityGraph("Order.withItems")
    @Query("select o from CustomerOrder o")
    List<CustomerOrder> findAllWithGraph();

    @Query("select distinct o from CustomerOrder o left join fetch o.items")
    List<CustomerOrder> findAllWithItems();
}
