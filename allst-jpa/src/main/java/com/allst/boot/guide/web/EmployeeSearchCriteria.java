package com.allst.boot.guide.web;

import com.allst.boot.guide.entity.EmployeeStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 复杂搜索条件。由自定义参数解析器从查询字符串绑定，不作为请求体。
 */
@Getter
@Setter
public class EmployeeSearchCriteria {

    private String name;
    private Integer minAge;
    private Integer maxAge;
    private EmployeeStatus status;
    private BigDecimal minSalary;
    private String departmentName;
}
