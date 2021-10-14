package com.allst.db.repository;

import com.allst.db.bean.Lagou;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author June
 * @since 2021年10月
 */
public interface LagouRepository extends MongoRepository<Lagou, String> {
    List<Lagou> findByNameEquals(String name);
    List<Lagou> findByNameAndSalary(String name, Double salary);
}
