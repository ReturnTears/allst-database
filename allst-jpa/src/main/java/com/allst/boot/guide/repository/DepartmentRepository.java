package com.allst.boot.guide.repository;

import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.repository.support.GuideBaseRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * detection-strategy=annotated 时，只有标注了 RepositoryRestResource 的仓库会挂到 /api 下面。
 */
@RepositoryRestResource(path = "departments", collectionResourceRel = "departments")
public interface DepartmentRepository extends GuideBaseRepository<Department, Long> {

    Optional<Department> findByName(String name);
}
