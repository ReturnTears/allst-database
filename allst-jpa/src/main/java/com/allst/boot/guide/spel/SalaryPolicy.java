package com.allst.boot.guide.spel;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 仓库 {@code @Query} 里的 {@code :#{@salaryPolicy.minSalary}} 会调用这个 Bean。
 */
@Component("salaryPolicy")
public class SalaryPolicy {

    private BigDecimal minSalary = new BigDecimal("1000.00");

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }
}
