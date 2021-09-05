package com.allst.mysql;

import com.allst.mysql.entity.City;
import com.allst.mysql.repository.CityRepository;
import org.apache.shardingsphere.api.hint.HintManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = AllstMysqlApplication.class)
class HintTests {

    @Autowired
    private CityRepository cityRepository;

    /**
     * 测试Hint强制修改路由规则
     */
    @Test
    void testFindAll() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.setDatabaseShardingValue(1L); //强制路由到ds${xx%2}
        List<City> cities = cityRepository.findAll();
        cities.forEach(System.out::println);
    }
}
