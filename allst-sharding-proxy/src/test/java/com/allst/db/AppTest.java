package com.allst.db;

import com.allst.db.entity.Position;
import com.allst.db.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Unit test for simple App.
 */
@SpringBootTest(classes = App.class)
public class AppTest {

    @Autowired
    private PositionRepository positionRepository;

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        List<Position> list = positionRepository.findAll();
        list.forEach(System.out::println);
    }

    /**
     * sharding-proxy 测试新增
     * 报错： Caused by: java.sql.SQLException: 2Unknown exception: [no table route info]
     * MySQL-CLI 执行新增也会报错
     */
    @Test
    public void test2() {
        Position position = new Position();
        position.setName("tiger");
        position.setSalary("30000");
        position.setCity("shanghai");
        positionRepository.save(position);
    }
}
