package com.allst.boot.guide.dto;

import com.allst.boot.guide.entity.Employee;
import lombok.Getter;

/**
 * JPQL 构造器投影的目标类型。类必须有匹配参数顺序的构造器，并且不能是实体。
 */
@Getter
public class EmployeeSummary {

    private final Long id;
    private final String name;
    private final String email;

    public EmployeeSummary(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static EmployeeSummary from(Employee employee) {
        return new EmployeeSummary(employee.getId(), employee.getName(), employee.getEmail());
    }
}
