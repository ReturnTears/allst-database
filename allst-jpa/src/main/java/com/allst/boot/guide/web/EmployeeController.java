package com.allst.boot.guide.web;

import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/guide/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Page<EmployeeSummary> page(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return employeeService.page(pageable);
    }

    @GetMapping("/sorted")
    public List<EmployeeSummary> sorted(Sort sort) {
        return employeeService.sorted(sort);
    }

    @GetMapping("/search")
    public List<EmployeeSummary> search(@BindEmployeeSearch EmployeeSearchCriteria criteria) {
        return employeeService.search(criteria);
    }

    /**
     * 服务方法返回后事务已经结束。open-in-view=true 时请求还持有 EntityManager，懒加载仍能取到部门名。
     * 这是为了演示 Session 对事务边界的影响，接口响应更适合直接返回 DTO。
     */
    @GetMapping("/{id}/department-name")
    public String departmentName(@PathVariable Long id) {
        Employee employee = employeeService.findOne(id);
        return employee.getDepartment().getName();
    }
}
