package com.allst.db.repository;

import com.allst.db.bean.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * @author June
 * @since 2021年11月
 */
@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {

}
