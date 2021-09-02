package com.allst.mysql;

import com.allst.mysql.entity.Position;
import com.allst.mysql.entity.PositionDetail;
import com.allst.mysql.repository.PositionDetailRepository;
import com.allst.mysql.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AllstMysqlApplication.class)
class AllstMysqlApplicationTests {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionDetailRepository positionDetailRepository;

    @Test
    void contextLoads() {
        for (int i = 0; i < 20; i++) {
            Position position = new Position();
            // position.setId(++i);
            position.setCity("GuangZhou");
            position.setName("zhangSan:" + i);
            position.setSalary("123456.78");
            positionRepository.save(position);
        }
    }

    @Test
    void testDetail() {
        for (int i = 0; i < 20; i++) {
            Position position = new Position();
            position.setCity("HangZhou");
            position.setName("Lisi:" + i);
            position.setSalary("391421.0");
            positionRepository.save(position);

            PositionDetail positionDetail = new PositionDetail();
            positionDetail.setPid(position.getId());
            positionDetail.setDescription("this is " + i + " list position detail record, Yes.");
            positionDetailRepository.save(positionDetail);
        }
    }

    @Test
    void test() {
        Object positions = positionRepository.findPositionsById(640312027549532160L);
        Object[] pos = (Object[]) positions;
        System.out.println(pos[0] + ", " + pos[1]);
    }
}
