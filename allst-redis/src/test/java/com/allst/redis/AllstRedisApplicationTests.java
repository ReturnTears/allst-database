package com.allst.redis;

import com.allst.redis.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

/**
 * redis常用功能测试类
 */
@SpringBootTest
class AllstRedisApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Test
    void contextLoads() {
        redisTemplate.opsForValue().set("addr", "重庆市渝北区");
    }

    @Test
    void redisLoads() {
        String addr = redisTemplate.opsForValue().get("addr");
        System.out.println(addr);
    }

    @Test
    void redisSetLoads() {
        redisUtil.set("k2", "Hello Redis ~");
    }

    @Test
    void redisSetLoads2() {
        String[] values = {"Hello", "World", "Redis"};
        boolean result = redisUtil.set2("k4", values);
        System.out.println(result);
    }

    @Test
    void redisKeysLoads() {
        Set<String> set = redisUtil.keys("*");
        assert set != null;
        set.forEach(System.out::println);
    }

    /**
     * 为已存在的键设置过期时间有效
     */
    @Test
    void redisExpireLoads() {
        boolean result = redisUtil.expire("k2", 30);
        System.out.println(result);
    }

    @Test
    void redisGetExpireLoads() {
        long result = redisUtil.getExpire("k2");
        System.out.println(result);
    }

    @Test
    void redisHasKeyLoads() {
        boolean result = redisUtil.hasKey("k2");
        System.out.println(result);
    }

    @Test
    void redisDelLoads() {
        redisUtil.del("k2", "k3");
    }
}
