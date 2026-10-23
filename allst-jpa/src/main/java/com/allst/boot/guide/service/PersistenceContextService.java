package com.allst.boot.guide.service;

import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.repository.EmployeeRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 事务范围的持久化上下文：同一个事务里相同主键只有一个实例。
 * 事务结束后实体脱离上下文，再碰懒加载属性会抛出 {@link LazyInitializationException}。
 */
@Service
public class PersistenceContextService {

    @PersistenceContext
    private EntityManager entityManager;

    private final EmployeeRepository employeeRepository;

    public PersistenceContextService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public boolean sameInstance(Long id) {
        Employee first = entityManager.find(Employee.class, id);
        Employee second = entityManager.find(Employee.class, id);
        return first == second;
    }

    @Transactional
    public boolean detachedAfterClear(Long id) {
        Employee employee = entityManager.find(Employee.class, id);
        entityManager.clear();
        return !entityManager.contains(employee);
    }

    @Transactional(readOnly = true)
    public Employee loadManaged(Long id) {
        return employeeRepository.findById(id).orElseThrow();
    }
}
