package com.allst.boot.guide.service;

import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.repository.DepartmentRepository;
import com.allst.boot.guide.repository.EmployeeRepository;
import com.allst.boot.guide.web.EmployeeSearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service 层单元测试：仓库全部是假的，只验证编排。
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void hireAssignsDepartmentAndActiveStatus() {
        Department department = new Department();
        department.setId(8L);
        department.setName("平台");
        when(departmentRepository.findById(8L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee hired = employeeService.hire("Ada", "ada@example.com", 32, new BigDecimal("1800.00"), 8L);

        assertEquals("Ada", hired.getName());
        assertEquals(EmployeeStatus.ACTIVE, hired.getStatus());
        assertEquals(department, hired.getDepartment());
        verify(employeeRepository).save(hired);
    }

    @Test
    void searchDelegatesCriteriaToCustomRepository() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Ada");
        employee.setEmail("ada@example.com");
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.setName("Ada");
        when(employeeRepository.search(criteria)).thenReturn(List.of(employee));

        assertEquals("ada@example.com", employeeService.search(criteria).get(0).getEmail());
    }
}
