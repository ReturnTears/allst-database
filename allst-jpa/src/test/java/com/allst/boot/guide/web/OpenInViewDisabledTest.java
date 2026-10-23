package com.allst.boot.guide.web;

import com.allst.boot.guide.GuideSliceApplication;
import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.repository.DepartmentRepository;
import com.allst.boot.guide.service.EmployeeService;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.servlet.ServletException;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 关掉 open-in-view 后，控制器里再访问懒加载部门会失败。
 */
@SpringBootTest(classes = GuideSliceApplication.class, properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc
class OpenInViewDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void lazyDepartmentFailsOutsideTransaction() throws Exception {
        String token = UUID.randomUUID().toString().substring(0, 8);
        Department department = new Department();
        department.setName("osiv-" + token);
        Long departmentId = departmentRepository.save(department).getId();
        Long employeeId = employeeService.hire("Ivy " + token, token + "@example.com", 29, new BigDecimal("1600.00"), departmentId)
                .getId();

        ServletException thrown = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/guide/employees/" + employeeId + "/department-name")));
        assertTrue(thrown.getCause() instanceof LazyInitializationException);
    }
}
