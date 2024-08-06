package com.allst.boot.repository.mongo;

import com.allst.boot.entity.PersonMongo;
import org.springframework.data.repository.Repository;

/**
 * @author Hutu
 * @since 2024-08-05 下午 10:29
 */
public interface MongoDBPersonRepository extends Repository<PersonMongo, Long> {
    PersonMongo findByName(String name);
}
