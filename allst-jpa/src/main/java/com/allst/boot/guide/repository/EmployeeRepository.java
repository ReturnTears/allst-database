package com.allst.boot.guide.repository;

import com.allst.boot.guide.dto.EmployeeNameView;
import com.allst.boot.guide.dto.EmployeeSummary;
import com.allst.boot.guide.dto.NameOnly;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.repository.support.GuideBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 一个仓库同时演示命名查询、返回值、@Query、Specification、Example、Querydsl 和自定义实现。
 * JpaRepository 已经继承 QueryByExampleExecutor，所以 Example 查询不用再声明接口。
 */
public interface EmployeeRepository extends GuideBaseRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee>,
        QuerydslPredicateExecutor<Employee>,
        EmployeeRepositoryCustom {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByDepartmentId(Long departmentId);

    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findByAgeBetweenAndStatus(Integer minAge, Integer maxAge, EmployeeStatus status);

    List<Employee> findByStatus(EmployeeStatus status);

    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    List<Employee> findByStatus(EmployeeStatus status, Sort sort);

    List<Employee> findTop5ByStatusOrderBySalaryDesc(EmployeeStatus status);

    Page<Employee> findByDepartmentName(String departmentName, Pageable pageable);

    List<Employee> findByIdIn(Collection<Long> ids);

    Stream<Employee> streamByStatus(EmployeeStatus status);

    List<NameOnly> findNameOnlyByStatus(EmployeeStatus status);

    @Query("select e.id as id, e.name as name from Employee e where e.department.id = :departmentId")
    List<EmployeeNameView> findNamesByDepartment(@Param("departmentId") Long departmentId);

    @Query("select new com.allst.boot.guide.dto.EmployeeSummary(e.id, e.name, e.email) "
            + "from Employee e where e.status = :status")
    List<EmployeeSummary> summarizeByStatus(@Param("status") EmployeeStatus status);

    @Query("select distinct e from Employee e join fetch e.department d where d.name = :name")
    List<Employee> findByDepartmentNameFetch(@Param("name") String name);

    @EntityGraph("Employee.details")
    Optional<Employee> findWithDetailsById(Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select e from Employee e where e.id = :id")
    Optional<Employee> findWithOptimisticLockById(@Param("id") Long id);

    @Query("select e from Employee e where e.salary >= :#{@salaryPolicy.minSalary}")
    List<Employee> findAbovePolicyFloor();

    @Query("select e from Employee e where lower(e.name) like lower(concat('%', :#{#keyword}, '%'))")
    List<Employee> searchBySpelName(@Param("keyword") String keyword);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Employee e set e.status = :status where e.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") EmployeeStatus status);

    @Query(value = "select * from g_employee where status = :status",
            countQuery = "select count(*) from g_employee where status = :status",
            nativeQuery = true)
    Page<Employee> pageByStatusNative(@Param("status") String status, Pageable pageable);

    default Optional<Employee> findActiveByEmail(String email) {
        return findByEmail(email).filter(employee -> employee.getStatus() == EmployeeStatus.ACTIVE);
    }
}
