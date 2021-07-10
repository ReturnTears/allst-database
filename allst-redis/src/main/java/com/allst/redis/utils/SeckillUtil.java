package com.allst.redis.utils;

import cn.hutool.core.util.StrUtil;
import redis.clients.jedis.Jedis;

/**
 * 秒杀工具类
 *
 * @author June
 * @since 2021年07月
 */
public class SeckillUtil {

    public static void main(String[] args) {

    }

    /**
     * 执行秒杀操作
     *
     * @param userId 用户id
     * @param prodId 商品id
     *
     * @return 结果
     */
    public static String doSecKill(String userId, String prodId) {
        // 1、用户和商品判断
        if (userId == null || prodId == null) {
            return "数据为空~";
        }
        // 2、连接redis
        final Jedis jedis = new Jedis("192.168.33.100", 6379);

        // 3、拼接key：库存key,用户key
        String kcKey = StrUtil.format("sk:{}:qt", prodId);
        String userKey = StrUtil.format("sk:{}:user", prodId);
        // 4、获取库存,为空则秒杀还未开始
        String kc = jedis.get(kcKey);
        if (kc == null) {
            String result = StrUtil.format("{}", "秒杀还未开始~");
            System.out.println(result);
            jedis.close();
            return result;
        }

        // 5、判断用户是否重读秒杀操作
        if (jedis.sismember(userKey, userId)) {
            String result = StrUtil.format("{}", "已经秒杀成功了，不能重复秒杀~");
            System.out.println(result);
            jedis.close();
            return result;
        }

        // 6、判断商品数量，小于1， 秒杀结束
        if (Integer.parseInt(kc) <= 0) {
            String result = StrUtil.format("{}", "秒杀已经结束了~");
            System.out.println(result);
            jedis.close();
            return result;
        }

        // 7、秒杀过程： 库存减1，用户加1
        jedis.decr(kcKey);
        jedis.sadd(userKey, userId);
        String result = StrUtil.format("{}:秒杀成功", userId);
        System.out.println(result);
        return result;
    }

}
