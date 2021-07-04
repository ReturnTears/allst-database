package com.allst.redis.utils;

import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * 验证码
 *
 * @author June
 * @since 2021年07月
 */
public class VerifyCode {

    final static Jedis jedis = new Jedis("192.168.33.100", 6379);

    public static void main(String[] args) {
        System.out.println(getRandomNumCode(6));
    }

    /**
     * 验证校验码
     *
     * @param phone 手机号
     * @param code  验证码
     */
    public static String getRedisCode(String phone, String code) {
        final String codeKey = "codesKey:" + phone + ":codes";
        String redisCode = jedis.get(codeKey);
        String resultMsg = "";
        if (redisCode.equals(code)) {
            resultMsg = "验证成功.";
            System.out.println(resultMsg);
            return resultMsg;
        } else {
            resultMsg = "验证失败.";
            System.out.println(resultMsg);
            return resultMsg;
        }
    }

    /**
     * 生成一个随机数验证码
     *
     * @return 验证码
     */
    public static String getRandomNumCode(final int num) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < num; i++) {
            code.append(random.nextInt(10));
        }
        System.out.println("Verify Code is : " + code.toString());
        return code.toString();
    }

    /**
     * 验证手机验证码
     *
     * @param phone 手机号
     */
    public static String getPhoneCode(String phone) {
        // 自定义key规则,发送次数存储的key,编码存储的key
        String countKey = "countKey:" + phone + ":count";
        String codesKey = "codesKey:" + phone + ":codes";
        String count = jedis.get(countKey);
        if (count == null) {
            jedis.setex(countKey, 24 * 60 * 60, "1");
        } else if (Integer.parseInt(count) < 3) {
            jedis.incr(countKey);
        } else if (Integer.parseInt(count) > 2) {
            System.out.println("今天发送次数已经超过3次");
            jedis.close();
        }

        // 存储验证码
        final String verifyCode = getRandomNumCode(6);
        jedis.set(codesKey, verifyCode);
        return count != null && Integer.parseInt(Objects.requireNonNull(count)) > 2 ? "手机号:" + phone + "，今天发送次数已经超过3次" : verifyCode;
    }
}
