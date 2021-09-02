package com.allst.mysql;

import com.allst.mysql.entity.BOrder;
import com.allst.mysql.entity.City;
import com.allst.mysql.entity.Position;
import com.allst.mysql.entity.PositionDetail;
import com.allst.mysql.repository.BOrderRepository;
import com.allst.mysql.repository.CityRepository;
import com.allst.mysql.repository.PositionDetailRepository;
import com.allst.mysql.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;

import java.util.Date;
import java.util.Random;

@SpringBootTest(classes = AllstMysqlApplication.class)
class AllstMysqlApplicationTests {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionDetailRepository positionDetailRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private BOrderRepository bOrderRepository;

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

    @Test
    void testBroadcast() {
        City city = new City();
        city.setName("ganshu");
        city.setProvince("jiayuguan");
        cityRepository.save(city);
    }

    /**
     * 测试分库分表
     * 注解Repeat(100) 失效 ？
     */
    @Test
    void testSharding() {
        for (int i = 0; i < 100; i++) {
            Random random = new Random();
            int companyId = random.nextInt(10);
            BOrder order = new BOrder();
            order.setDel(false);
            order.setCompanyId(companyId);
            order.setPositionId(3242342);
            order.setUserId(2222);
            order.setPublishUserId(1111);
            order.setResumeType(1);
            order.setStatus("AUTO");
            order.setCreateTime(new Date());
            order.setOperateTime(new Date());
            order.setWorkYear("2");
            order.setName("Boss Zhi Pin");
            order.setPositionName("Java");
            order.setResumeId(23233);

            bOrderRepository.save(order);
        }
    }
}
