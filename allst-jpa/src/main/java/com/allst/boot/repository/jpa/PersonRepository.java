package com.allst.boot.repository.jpa;

import com.allst.boot.entity.Person;

/**
 * @author Hutu
 * @since 2024-08-05 下午 09:37
 */
public interface PersonRepository extends MyBaseRepository<Person, Long> {
    Person findByName(String name);
}
