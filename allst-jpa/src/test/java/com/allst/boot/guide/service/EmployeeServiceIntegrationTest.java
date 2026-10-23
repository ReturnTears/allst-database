package com.allst.boot.guide.service;

import com.allst.boot.guide.GuideSliceApplication;
import com.allst.boot.guide.config.AuditorAwareImpl;
import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.repository.DepartmentRepository;
import com.allst.boot.guide.repository.EmployeeRepository;
import com.allst.boot.guide.web.EmployeeSearchCriteria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service 层集成测试：真实 H2、真实事务和真实仓库。
 */
@SpringBootTest(classes = GuideSliceApplication.class)
class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @AfterEach
    void clearAuditor() {
        AuditorAwareImpl.CURRENT.remove();
    }

    @Test
    void hirePersistsAndSupportsExampleSpecificationAndQuerydsl() {
        AuditorAwareImpl.CURRENT.set("service-it");
        String token = UUID.randomUUID().toString().substring(0, 8);
        var department = new com.allst.boot.guide.entity.Department();
        department.setName("svc-" + token);
        Long departmentId = departmentRepository.save(department).getId();

        Employee hired = employeeService.hire(
                "Ada " + token,
                token + "@example.com",
                28,
                new BigDecimal("3200.00"),
                departmentId);

        assertEquals("service-it", employeeRepository.findById(hired.getId()).orElseThrow().getCreatedBy());

        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.setName(token);
        criteria.setStatus(EmployeeStatus.ACTIVE);
        criteria.setDepartmentName("svc-" + token);

        List<EmployeeSummary> searched = employeeService.search(criteria);
        assertEquals(1, searched.size());
        assertEquals(1, employeeService.bySpecification(criteria).size());
        assertFalse(copy(employeeService.byQuerydsl(token, EmployeeStatus.ACTIVE)).isEmpty());
        assertEquals(1, employeeService.byExample(token, EmployeeStatus.ACTIVE).size());
        assertTrue(employeeService.page(org.springframework.data.domain.PageRequest.of(0, 200,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")))
                .stream().anyMatch(summary -> summary.getEmail().equals(token + "@example.com")));
    }

    private List<Employee> copy(Iterable<Employee> employees) {
        List<Employee> list = new java.util.ArrayList<>();
        employees.forEach(list::add);
        return list;
    }
}
