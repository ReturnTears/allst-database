package com.allst.boot.guide.service;

import com.allst.boot.guide.common.NotFoundException;
import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.repository.DepartmentRepository;
import com.allst.boot.guide.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 独立 Bean，保证 REQUIRES_NEW 经过事务代理。同一个类里的自调用不会开启新事务。
 */
@Service
public class TransactionPropagationWorker {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public TransactionPropagationWorker(DepartmentRepository departmentRepository,
                                        EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Employee saveInNewTransaction(Long departmentId, String name, String email) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("部门不存在: " + departmentId));
        return employeeRepository.save(employee(department, name, email));
    }

    private Employee employee(Department department, String name, String email) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setAge(30);
        employee.setSalary(new BigDecimal("2000.00"));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDepartment(department);
        return employee;
    }
}
