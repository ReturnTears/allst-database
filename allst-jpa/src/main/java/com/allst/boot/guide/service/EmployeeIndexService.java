package com.allst.boot.guide.service;

import com.allst.boot.guide.entity.Employee;
import com.allst.boot.guide.es.EmployeeDocument;
import com.allst.boot.guide.es.EmployeeSearchRepository;
import com.allst.boot.guide.repository.EmployeeRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 只有 elasticsearch profile 会创建搜索仓库。JPA 保存成功后再写索引，两边的模型互不继承。
 */
@Service
@Profile("elasticsearch")
public class EmployeeIndexService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeSearchRepository employeeSearchRepository;

    public EmployeeIndexService(EmployeeRepository employeeRepository,
                                EmployeeSearchRepository employeeSearchRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeSearchRepository = employeeSearchRepository;
    }

    @Transactional
    public Employee saveAndIndex(Employee employee) {
        Employee saved = employeeRepository.save(employee);
        employeeSearchRepository.save(EmployeeDocument.from(saved));
        return saved;
    }
}
