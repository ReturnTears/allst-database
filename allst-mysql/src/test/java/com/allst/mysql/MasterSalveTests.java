package com.allst.mysql;

import com.allst.mysql.entity.City;
import com.allst.mysql.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = AllstMysqlApplication.class)
class MasterSalveTests {

    @Autowired
    private CityRepository cityRepository;

    /**
     * 测试主从新增City，是新增到主库
     */
    @Test
    void testAddCity() {
        City city = new City();
        city.setName("xizang");
        city.setProvince("lasha");
        cityRepository.save(city);
    }

    /**
     * 测试主从架构 读取数据是从从库读取
     */
    @Test
    void testFindAll() {
        List<City> cities = cityRepository.findAll();
        cities.forEach(System.out::println);
    }
}
