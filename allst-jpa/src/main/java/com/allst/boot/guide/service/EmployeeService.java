package com.allst.boot.guide.service;

import com.allst.boot.guide.common.NotFoundException;
import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.entity.QEmployee;
import com.allst.boot.guide.repository.DepartmentRepository;
import com.allst.boot.guide.repository.EmployeeRepository;
import com.allst.boot.guide.repository.spec.EmployeeSpecifications;
import com.allst.boot.guide.web.EmployeeSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Employee hire(String name, String email, Integer age, BigDecimal salary, Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("部门不存在: " + departmentId));
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setAge(age);
        employee.setSalary(salary);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDepartment(department);
        return employeeRepository.save(employee);
    }

    public Page<EmployeeSummary> page(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(EmployeeSummary::from);
    }

    public List<EmployeeSummary> search(EmployeeSearchCriteria criteria) {
        return employeeRepository.search(criteria).stream()
                .map(EmployeeSummary::from)
                .collect(Collectors.toList());
    }

    public List<EmployeeSummary> sorted(Sort sort) {
        Sort safeSort = sort == null || sort.isUnsorted() ? Sort.by("id") : sort;
        return employeeRepository.findAll(safeSort).stream()
                .map(EmployeeSummary::from)
                .collect(Collectors.toList());
    }

    public Employee findOne(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("员工不存在: " + id));
    }

    public List<Employee> byExample(String name, EmployeeStatus status) {
        Employee probe = new Employee();
        probe.setName(name);
        probe.setStatus(status);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("version", "projects", "profile")
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        probe.setProjects(null);
        probe.setProfile(null);
        Iterable<Employee> found = employeeRepository.findAll(Example.of(probe, matcher));
        List<Employee> employees = new ArrayList<>();
        found.forEach(employees::add);
        return employees;
    }

    public List<Employee> bySpecification(EmployeeSearchCriteria criteria) {
        return employeeRepository.findAll(EmployeeSpecifications.from(criteria));
    }

    public Iterable<Employee> byQuerydsl(String name, EmployeeStatus status) {
        QEmployee employee = QEmployee.employee;
        BooleanBuilder where = new BooleanBuilder();
        if (name != null && !name.isBlank()) {
            where.and(employee.name.containsIgnoreCase(name));
        }
        if (status != null) {
            where.and(employee.status.eq(status));
        }
        return employeeRepository.findAll(where, Sort.by("id"));
    }

    @Transactional
    public int changeStatus(Long id, EmployeeStatus status) {
        findOne(id);
        return employeeRepository.updateStatus(id, status);
    }
}
