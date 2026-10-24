package com.allst.boot.order.repo;

import com.allst.boot.order.entity.MallOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<MallOrder, Long>, JpaSpecificationExecutor<MallOrder> {

    Optional<MallOrder> findByOrderNo(String orderNo);

    boolean existsByCustomer_Id(Long customerId);

    List<MallOrder> findByCustomer_IdIn(Collection<Long> customerIds);
}
