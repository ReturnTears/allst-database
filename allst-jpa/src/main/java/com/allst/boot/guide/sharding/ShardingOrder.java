package com.allst.boot.guide.sharding;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 分表实体使用应用分配的主键。跨物理表的 IDENTITY 会让 Hibernate 和 ShardingSphere 抢回填主键。
 */
@Getter
@Setter
@Entity
@Table(name = "g_shard_order")
public class ShardingOrder {

    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
