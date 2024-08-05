package com.allst.boot.repository.jpa;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * @author Hutu
 * @since 2024-08-05 下午 09:36
 */
@NoRepositoryBean
public interface MyBaseRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findById(ID id);
    <S extends T> S save(S entity);
}
