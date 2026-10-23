package com.allst.boot.guide.repository;

import com.allst.boot.guide.GuideSliceApplication;
import com.allst.boot.guide.config.AuditorAwareImpl;
import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.entity.Book;
import com.allst.boot.guide.entity.Customer;
import com.allst.boot.guide.entity.CustomerOrder;
import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.entity.DigitalGoods;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeProfile;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.entity.Project;
import com.allst.boot.guide.repository.support.GuideBaseRepository;
import com.allst.boot.guide.service.AsyncEmployeeService;
import com.allst.boot.guide.service.CustomerService;
import com.allst.boot.guide.service.OrderQueryService;
import com.allst.boot.guide.service.PersistenceContextService;
import com.allst.boot.guide.service.QueryPlanCacheService;
import com.allst.boot.guide.service.TransactionPropagationService;
import com.allst.boot.guide.spel.SpelGuide;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = GuideSliceApplication.class)
class GuideJpaBehaviorTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private PersistenceContextService persistenceContextService;

    @Autowired
    private AsyncEmployeeService asyncEmployeeService;

    @Autowired
    private QueryPlanCacheService queryPlanCacheService;

    @Autowired
    private TransactionPropagationService transactionPropagationService;

    @Autowired
    private SpelGuide spelGuide;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void clearAuditor() {
        AuditorAwareImpl.CURRENT.remove();
    }

    @Test
    void namingQueryReturnValuesSpelAndRefresh() {
        String token = UUID.randomUUID().toString().substring(0, 8);
        Department department = department("dept-" + token);
        Employee employee = employee(department, "Query " + token, token + "@example.com", new BigDecimal("2500.00"));
        employee = employeeRepository.save(employee);

        assertTrue(employeeRepository.existsByEmail(token + "@example.com"));
        assertEquals(1, employeeRepository.findByAgeBetweenAndStatus(20, 40, EmployeeStatus.ACTIVE).stream()
                .filter(item -> item.getEmail().equals(token + "@example.com")).count());
        assertFalse(employeeRepository.findTop5ByStatusOrderBySalaryDesc(EmployeeStatus.ACTIVE).isEmpty());
        assertEquals(1, employeeRepository.summarizeByStatus(EmployeeStatus.ACTIVE).stream()
                .map(EmployeeSummary::getEmail).filter(email -> email.equals(token + "@example.com")).count());
        assertFalse(employeeRepository.findNameOnlyByStatus(EmployeeStatus.ACTIVE).isEmpty());
        assertEquals(1, employeeRepository.searchBySpelName(token).size());
        assertTrue(employeeRepository.findAbovePolicyFloor().stream()
                .anyMatch(item -> item.getEmail().equals(token + "@example.com")));
        assertTrue(spelGuide.salaryAboveFloor(employee));
        assertEquals(new BigDecimal("1000.00"), spelGuide.floor());

        Long id = employee.getId();
        transactionTemplate.executeWithoutResult(status -> {
            Employee managed = employeeRepository.findById(id).orElseThrow();
            entityManager.createNativeQuery("update g_employee set emp_name = :name where id = :id")
                    .setParameter("name", "Refreshed " + token)
                    .setParameter("id", id)
                    .executeUpdate();
            employeeRepository.refresh(managed);
            assertEquals("Refreshed " + token, managed.getName());
        });

        assertTrue(employeeRepository instanceof GuideBaseRepository);
    }

    @Test
    void associationsInheritanceCallbacksAndQueryPlan() throws Exception {
        String token = UUID.randomUUID().toString().substring(0, 8);
        Department department = department("rel-" + token);
        Employee employee = employee(department, "Rel " + token, "rel-" + token + "@example.com", new BigDecimal("1800.00"));
        EmployeeProfile profile = new EmployeeProfile();
        profile.setPhone("13900000000");
        profile.setAddress("Shanghai");
        employee.attachProfile(profile);
        Project project = new Project();
        project.setCode("p-" + token);
        project.setTitle("Guide");
        projectRepository.save(project);
        employee.addProject(project);
        employee = employeeRepository.save(employee);
        Long employeeId = employee.getId();

        Employee loaded = employeeRepository.findWithDetailsById(employeeId).orElseThrow();
        assertEquals("Shanghai", loaded.getProfile().getAddress());
        assertEquals(1, loaded.getProjects().size());
        assertEquals("rel-" + token, asyncEmployeeService.loadDepartmentName(employeeId).get(5, TimeUnit.SECONDS));

        assertTrue(persistenceContextService.sameInstance(employeeId));
        assertTrue(persistenceContextService.detachedAfterClear(employeeId));
        Employee detached = transactionTemplate.execute(status -> employeeRepository.findById(employeeId).orElseThrow());
        assertThrows(LazyInitializationException.class, () -> detached.getDepartment().getName());

        Book book = new Book();
        book.setName("Inherited " + token);
        book.setPrice(new BigDecimal("20.00"));
        book.setIsbn("isbn-" + token);
        book.setAuthor("Grace");
        productRepository.save(book);
        DigitalGoods goods = new DigitalGoods();
        goods.setName("Inherited " + token);
        goods.setPrice(new BigDecimal("5.00"));
        goods.setDownloadUrl("https://example.com");
        goods.setLicenseDays(7);
        productRepository.save(goods);
        assertEquals(2, productRepository.findByNameContaining("Inherited " + token).size());
        assertTrue(productRepository.findBookByIsbn("isbn-" + token).isPresent());

        Customer customer = customerService.register("Callback " + token, "secret", LocalDate.of(2000, 5, 1), null);
        assertEquals("Callback " + token, customer.getNickname());
        assertTrue(customerService.get(customer.getId()).isLoaded());
        assertFalse(auditTrailRepository.findByEntityNameAndAction("Customer", "PERSIST").isEmpty());

        for (int i = 0; i < 6; i++) {
            CustomerOrder order = new CustomerOrder();
            order.setOrderNo(token + "-" + i);
            order.setCustomerName("buyer");
            order.addItem("sku", 1, new BigDecimal("3.00"));
            customerOrderRepository.save(order);
        }
        queryPlanCacheService.statistics().clear();
        int batched = orderQueryService.countItemsNaive();
        long batchedStatements = queryPlanCacheService.statistics().getPrepareStatementCount();
        queryPlanCacheService.statistics().clear();
        int fetched = orderQueryService.countItemsFetchJoin();
        long fetchedStatements = queryPlanCacheService.statistics().getPrepareStatementCount();
        assertEquals(batched, fetched);
        assertTrue(fetched >= 6);
        assertTrue(batchedStatements > fetchedStatements);

        assertFalse(orderQueryService.pageWithItems(PageRequest.of(0, 3, Sort.by("id"))).isEmpty());

        long hitsBefore = queryPlanCacheService.hitCount();
        employeeRepository.findByStatus(EmployeeStatus.ACTIVE, Sort.by("id"));
        employeeRepository.findByStatus(EmployeeStatus.ACTIVE, Sort.by("id"));
        assertTrue(queryPlanCacheService.hitCount() > hitsBefore);
        employeeRepository.findByIdIn(java.util.List.of(employeeId, employeeId));

        Long departmentId = department.getId();
        String kept = "kept-" + token + "@example.com";
        String rolled = "rolled-" + token + "@example.com";
        assertThrows(IllegalStateException.class,
                () -> transactionPropagationService.outerFailsAfterInnerCommit(departmentId, kept, rolled));
        assertTrue(employeeRepository.findByEmail(kept).isPresent());
        assertFalse(employeeRepository.findByEmail(rolled).isPresent());
    }

    private Department department(String name) {
        Department department = new Department();
        department.setName(name);
        return departmentRepository.save(department);
    }

    private Employee employee(Department department, String name, String email, BigDecimal salary) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setAge(30);
        employee.setSalary(salary);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDepartment(department);
        return employee;
    }
}
