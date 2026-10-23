package com.allst.boot.guide.service;

import com.allst.boot.guide.common.NotFoundException;
import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.repository.DepartmentRepository;
import com.allst.boot.guide.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 外层事务回滚不会撤销 REQUIRES_NEW 已经提交的工作。
 * 连接在每个事务结束时归还连接池。
 */
@Service
public class TransactionPropagationService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final TransactionPropagationWorker worker;

    public TransactionPropagationService(DepartmentRepository departmentRepository,
                                         EmployeeRepository employeeRepository,
                                         TransactionPropagationWorker worker) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.worker = worker;
    }

    @Transactional
    public void outerFailsAfterInnerCommit(Long departmentId, String keptEmail, String rolledEmail) {
        worker.saveInNewTransaction(departmentId, "kept", keptEmail);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("部门不存在: " + departmentId));
        employeeRepository.save(employee(department, "rolled", rolledEmail));
        throw new IllegalStateException("外层事务失败");
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
