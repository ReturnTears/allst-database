package com.allst.mysql.repository;

import com.allst.mysql.entity.BOrder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * BOrderRepository
 *
 * @author June
 * @since 2021年09月
 */
public interface BOrderRepository extends JpaRepository<BOrder, Long> {

}
