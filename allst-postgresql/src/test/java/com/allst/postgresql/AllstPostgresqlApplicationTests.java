package com.allst.postgresql;

import com.allst.postgresql.entity.UserInfo;
import com.allst.postgresql.reponsitory.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
class AllstPostgresqlApplicationTests {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Test
    void contextLoads() {
        List<String> list = List.of("AA", "BB", "CC", "DD", "EE", "FF");
        AtomicInteger count = new AtomicInteger(1);
        list.forEach(name -> {
            UserInfo userInfo = new UserInfo();
            userInfo.setName(name);
            userInfo.setAge(20 + count.getAndIncrement());
            userInfoRepository.save(userInfo);
        });
    }

    @Test
    void testFindByName() {
        UserInfo userInfo = userInfoRepository.findByName("AA");
        log.info("userInfo: {}", userInfo);
    }

    @Test
    void testFindByNameAndAge() {
        UserInfo userInfo = userInfoRepository.findByNameAndAge("AA", 21);
        log.info("userInfo: {}", userInfo);
    }

    @Test
    void testFindUser() {
        UserInfo userInfo = userInfoRepository.findUser("AA");
        log.info("userInfo: {}", userInfo);
    }
}
