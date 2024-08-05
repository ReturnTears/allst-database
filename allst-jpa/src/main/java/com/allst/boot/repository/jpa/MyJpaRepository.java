package com.allst.boot.repository.jpa;

import com.allst.boot.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Hutu
 * @since 2024-08-05 下午 10:21
 */
public interface MyJpaRepository extends JpaRepository<Person, Long> {
}
