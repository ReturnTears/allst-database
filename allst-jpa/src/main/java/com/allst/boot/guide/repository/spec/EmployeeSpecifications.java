package com.allst.boot.guide.repository.spec;

import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.entity.EmployeeStatus;
import com.allst.boot.guide.web.EmployeeSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Specification 可以组合。调用方决定本次请求到底叠哪些条件。
 */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> from(EmployeeSearchCriteria criteria) {
        EmployeeSearchCriteria safe = criteria == null ? new EmployeeSearchCriteria() : criteria;
        return Specification.where(nameContains(safe.getName()))
                .and(statusIs(safe.getStatus()))
                .and(salaryAtLeast(safe.getMinSalary()))
                .and(departmentName(safe.getDepartmentName()));
    }

    public static Specification<Employee> nameContains(String name) {
        return (root, query, builder) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Employee> statusIs(EmployeeStatus status) {
        return (root, query, builder) -> status == null ? null : builder.equal(root.get("status"), status);
    }

    public static Specification<Employee> salaryAtLeast(BigDecimal minSalary) {
        return (root, query, builder) -> minSalary == null ? null : builder.greaterThanOrEqualTo(root.get("salary"), minSalary);
    }

    public static Specification<Employee> departmentName(String departmentName) {
        return (root, query, builder) -> {
            if (departmentName == null || departmentName.isBlank()) {
                return null;
            }
            return builder.equal(root.join("department").get("name"), departmentName);
        };
    }
}
