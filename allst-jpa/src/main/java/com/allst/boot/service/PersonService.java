package com.allst.boot.service;

import com.allst.boot.entity.Person;
import com.allst.boot.model.PersonBo;

/**
 * @author Hutu
 * @since 2024-08-05 下午 09:41
 */
public interface PersonService {
    void save(PersonBo personBo);
    PersonBo findById(Long id);
    PersonBo findByName(String name);
    Person findPersonByName(String name);
}
