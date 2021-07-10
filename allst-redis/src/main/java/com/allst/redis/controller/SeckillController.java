package com.allst.redis.controller;

import com.allst.redis.utils.SeckillUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * @author June
 * @since 2021年07月
 */
@RestController
@RequestMapping("/sec")
public class SeckillController {

    /**
     * 商品秒杀请求
     *
     * @param prodId 商品id
     * @return 结果
     */
    @RequestMapping("/dt/{prodId}")
    public String getSeckillDt(@PathVariable("prodId") String prodId) {
        // 模拟用户随机生成一个id
        String userId = new Random().nextInt(50000) + "";
        return SeckillUtil.doSecKill(userId, prodId);
    }

}
