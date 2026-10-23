package com.allst.boot.guide.spel;

import com.allst.boot.guide.entity.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 业务代码里直接使用 SpEL。表达式应来自受控配置，不要执行用户提交的字符串。
 */
@Component
public class SpelGuide {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final SalaryPolicy salaryPolicy;

    /**
     * 容器在创建本 Bean 时解析表达式，读取另一个 Bean 的属性。
     */
    @Value("#{salaryPolicy.minSalary}")
    private BigDecimal configuredFloor;

    public SpelGuide(SalaryPolicy salaryPolicy) {
        this.salaryPolicy = salaryPolicy;
    }

    public boolean salaryAboveFloor(Employee employee) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("employee", employee);
        context.setVariable("floor", salaryPolicy.getMinSalary());
        Boolean matched = parser.parseExpression("#employee.salary != null and #employee.salary >= #floor")
                .getValue(context, Boolean.class);
        return Boolean.TRUE.equals(matched);
    }

    public BigDecimal floor() {
        return configuredFloor;
    }
}
