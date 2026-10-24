package com.allst.boot.catalog.repo;

import com.allst.boot.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByCategory_Id(Long categoryId);

    @Query("select p.category.id, count(p) from Product p where p.category.id in :ids group by p.category.id")
    List<Object[]> countGrouped(@Param("ids") Collection<Long> ids);
}
