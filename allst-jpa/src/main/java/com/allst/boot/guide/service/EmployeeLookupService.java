package com.allst.boot.guide.service;

import com.allst.boot.guide.common.NotFoundException;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 异步线程里要再进一次 Spring Bean，@Transactional 才会在那条线程上打开持久化上下文。
 * 不要在 @Async 方法上再叠 @Transactional，异步拦截器会绕过同一个类上的事务代理。
 */
@Service
public class EmployeeLookupService {

    private final EmployeeRepository employeeRepository;

    public EmployeeLookupService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public String departmentName(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("员工不存在: " + employeeId));
        return employee.getDepartment().getName();
    }
}
