package com.allst.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis常用功能测试类
 */
@SpringBootTest
class AllstRedisApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        redisTemplate.opsForValue().set("addr", "重庆市渝北区");
    }

}
