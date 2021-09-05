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
}
