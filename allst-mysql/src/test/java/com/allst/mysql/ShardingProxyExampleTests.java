package com.allst.mysql;

import com.allst.mysql.entity.Position;
import com.allst.mysql.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author June
 * @since 2021年09月
 */
@SpringBootTest(classes = AllstMysqlApplication.class)
public class ShardingProxyExampleTests {
    @Autowired
    private PositionRepository positionRepository;

    @Test
    public void testTX() {
        List<Position> positions = positionRepository.findAll();
        positions.forEach(System.out::println);
    }
}
