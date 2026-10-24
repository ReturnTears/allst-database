package com.allst.boot.order.repo;

import com.allst.boot.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);

    List<OrderItem> findByOrder_IdIn(Collection<Long> orderIds);

    boolean existsByProduct_Id(Long productId);
}
