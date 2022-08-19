package com.allst.redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author June
 * @since 2021年07月
 */
@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/test1")
    public String getRedisValue() {
        redisTemplate.opsForValue().set("name", "YangYang");
        return (String) redisTemplate.opsForValue().get("name");
    }

    @GetMapping("/test2")
    public String getRedisValue2() {
        Object test2 = redisTemplate.opsForValue().get("test2");
        System.out.println(test2);
        return (String) redisTemplate.opsForValue().get("test2");
    }
}
