package com.allst.redis.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author YiYa
 * @since 2019-12-30 下午 11:58
 */
@RestController
public class IndexController {

    @Autowired
    private Redisson redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/indexStock")
    public String indexStock() {
        String lockKey = "product";
        String clientID = UUID.randomUUID().toString();
        RLock rLock = redisson.getLock(lockKey);    // 获取锁的对象
        try {
            redisTemplate.opsForValue().setIfAbsent(lockKey, clientID, 30, TimeUnit.SECONDS);

            rLock.lock(30, TimeUnit.SECONDS);// 在执行32行代码时, 会在这行代码开启一个分线程执行续命逻辑

            int stock = Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get("stock")));     // jedis.get("stock")
            if (stock > 0) {
                int realStock = stock - 1;
                redisTemplate.opsForValue().set("stock", realStock + "");   //  jedis.set("stock")
                System.out.println("扣减成功, 剩余库存: " + realStock + "");
            } else {
                System.out.println("扣减失败, 库存不足");
            }
        } finally {
            if (clientID.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.delete(lockKey);
            }
            rLock.unlock();     // 释放锁
        }
        return "Success";
    }

    @RequestMapping("/index")
    public String index() {
        return "Hello Redis";
    }
}
