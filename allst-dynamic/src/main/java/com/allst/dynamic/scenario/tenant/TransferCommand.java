package com.allst.dynamic.scenario.tenant;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferCommand {
    private String fromTenant;
    private String toTenant;
    private BigDecimal amount;
    private String bizNo;
    private boolean failAfterDebit;
}
