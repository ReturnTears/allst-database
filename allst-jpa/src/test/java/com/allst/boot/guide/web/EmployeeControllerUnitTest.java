package com.allst.boot.guide.web;

import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller 层单元测试：不启动 Spring 容器，只验证参数绑定和状态码。
 */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerUnitTest {

    @Mock
    private EmployeeService employeeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EmployeeController(employeeService))
                .setCustomArgumentResolvers(new EmployeeSearchArgumentResolver(), new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void pageUsesPageableResolver() throws Exception {
        when(employeeService.page(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new EmployeeSummary(1L, "Ada", "ada@example.com"))));

        mockMvc.perform(get("/guide/employees").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ada"));
    }

    @Test
    void searchUsesCustomArgumentResolver() throws Exception {
        when(employeeService.search(any(EmployeeSearchCriteria.class)))
                .thenReturn(List.of(new EmployeeSummary(2L, "Grace", "grace@example.com")));

        mockMvc.perform(get("/guide/employees/search")
                        .param("name", "Grace")
                        .param("minAge", "20")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("grace@example.com"));

        ArgumentCaptor<EmployeeSearchCriteria> captor = ArgumentCaptor.forClass(EmployeeSearchCriteria.class);
        verify(employeeService).search(captor.capture());
        assertEquals("Grace", captor.getValue().getName());
        assertEquals(20, captor.getValue().getMinAge());
        assertEquals(EmployeeStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void unknownStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/guide/employees/search").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
}
