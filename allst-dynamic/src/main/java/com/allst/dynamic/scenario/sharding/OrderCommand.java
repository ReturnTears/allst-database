package com.allst.dynamic.scenario.sharding;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCommand {
    private Long userId;
    private String orderNo;
    private BigDecimal amount;
}
