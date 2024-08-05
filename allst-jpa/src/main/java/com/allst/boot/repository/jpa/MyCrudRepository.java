package com.allst.boot.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author Hutu
 * @since 2024-08-05 下午 10:24
 */
@NoRepositoryBean
public interface MyCrudRepository<T, ID> extends CrudRepository<T, ID> {

}
