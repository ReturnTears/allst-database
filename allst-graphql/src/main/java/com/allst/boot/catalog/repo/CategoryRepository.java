package com.allst.boot.catalog.repo;

import com.allst.boot.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByIdAsc();

    List<Category> findByParent_IdIn(Collection<Long> parentIds);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByParent_Id(Long parentId);
}
