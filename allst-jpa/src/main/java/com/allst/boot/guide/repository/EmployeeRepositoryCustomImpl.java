package com.allst.boot.guide.repository;

import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.web.EmployeeSearchCriteria;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * 命名查询和 @Query 覆盖不了的动态条件，用 Criteria API 在自定义实现里拼。
 */
public class EmployeeRepositoryCustomImpl implements EmployeeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Employee> search(EmployeeSearchCriteria criteria) {
        EmployeeSearchCriteria safe = criteria == null ? new EmployeeSearchCriteria() : criteria;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> query = builder.createQuery(Employee.class);
        Root<Employee> root = query.from(Employee.class);
        List<Predicate> predicates = new ArrayList<>();

        if (hasText(safe.getName())) {
            predicates.add(builder.like(builder.lower(root.get("name")), "%" + safe.getName().toLowerCase() + "%"));
        }
        if (safe.getMinAge() != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("age"), safe.getMinAge()));
        }
        if (safe.getMaxAge() != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("age"), safe.getMaxAge()));
        }
        if (safe.getStatus() != null) {
            predicates.add(builder.equal(root.get("status"), safe.getStatus()));
        }
        if (safe.getMinSalary() != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("salary"), safe.getMinSalary()));
        }
        if (hasText(safe.getDepartmentName())) {
            Join<Employee, Department> department = root.join("department", JoinType.INNER);
            predicates.add(builder.equal(department.get("name"), safe.getDepartmentName()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(builder.asc(root.get("id")));
        return entityManager.createQuery(query).getResultList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
