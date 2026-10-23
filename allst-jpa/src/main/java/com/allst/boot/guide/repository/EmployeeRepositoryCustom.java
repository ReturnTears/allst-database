package com.allst.boot.guide.repository;

import com.allst.boot.guide.web.EmployeeSearchCriteria;
import com.allst.boot.guide.entity.Employee;

import java.util.List;

/**
 * 自定义仓库片段。Spring Data 会寻找同包下的 {@code EmployeeRepositoryCustomImpl} 并拼进代理。
 */
public interface EmployeeRepositoryCustom {

    List<Employee> search(EmployeeSearchCriteria criteria);
}
