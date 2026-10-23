package com.allst.boot.guide.web;

import com.allst.boot.guide.GuideSliceApplication;
import com.allst.boot.guide.entity.Book;
import com.allst.boot.guide.entity.Customer;
import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.entity.DigitalGoods;
import com.allst.boot.guide.repository.DepartmentRepository;
import com.allst.boot.guide.repository.ProductRepository;
import com.allst.boot.guide.service.CustomerService;
import com.allst.boot.guide.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller 层集成测试：MockMvc 走完整 DispatcherServlet，数据库是 H2。
 */
@SpringBootTest(classes = GuideSliceApplication.class)
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void searchAndDepartmentNameGoThroughMvc() throws Exception {
        String token = UUID.randomUUID().toString().substring(0, 8);
        Department department = new Department();
        department.setName("web-" + token);
        Long departmentId = departmentRepository.save(department).getId();
        Long employeeId = employeeService.hire("Grace " + token, token + "@example.com", 36, new BigDecimal("1500.00"), departmentId)
                .getId();

        mockMvc.perform(get("/guide/employees/search").param("name", token).param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(token + "@example.com"));

        mockMvc.perform(get("/guide/employees/" + employeeId + "/department-name"))
                .andExpect(status().isOk())
                .andExpect(content().string("web-" + token));
    }

    @Test
    void jacksonHidesPasswordAndPolymorphicProductExposesKind() throws Exception {
        String token = UUID.randomUUID().toString().substring(0, 8);
        Customer customer = customerService.register("Lin " + token, "secret", LocalDate.of(1990, 1, 2), "13800000000");

        mockMvc.perform(get("/guide/customers/" + customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer_name").value("Lin " + token))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.birthday").value("1990-01-02"));

        mockMvc.perform(get("/guide/customers/" + customer.getId() + "/internal"))
                .andExpect(jsonPath("$.phone").value("13800000000"));

        Book book = new Book();
        book.setName("Book " + token);
        book.setPrice(new BigDecimal("12.00"));
        book.setIsbn("isbn-" + token);
        book.setAuthor("Ada");
        productRepository.save(book);

        DigitalGoods goods = new DigitalGoods();
        goods.setName("Digital " + token);
        goods.setPrice(new BigDecimal("8.00"));
        goods.setDownloadUrl("https://example.com/" + token);
        goods.setLicenseDays(30);
        productRepository.save(goods);

        mockMvc.perform(get("/guide/products").param("name", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.kind == 'book')].isbn").exists())
                .andExpect(jsonPath("$[?(@.kind == 'digital')].downloadUrl").exists());

        mockMvc.perform(post("/api/departments")
                        .contentType("application/json")
                        .content("{\"name\":\"rest-" + token + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
    }
}
