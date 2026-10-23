package com.allst.boot.guide.sharding;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShardingOrderRepository extends JpaRepository<ShardingOrder, Long> {
}
