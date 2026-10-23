package com.allst.dynamic.scenario.tenant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_ledger")
public class LedgerEntry {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String tenantId;
    private String direction;
    private BigDecimal amount;
    private String bizNo;
    private LocalDateTime createdAt;
}
