package com.allst.dynamic.scenario.sharding;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class TradeOrder {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String orderNo;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
