package com.allst.redis.service;

/**
 * @author Hutu
 * @since 2023-06-05 下午 11:07
 */
public interface RedisService {
    /**
     * 更新时效性数据--String类型
     **/
    Boolean updateValidValue(String key, String value);

    /**
     * 判断当前用户是否达到访问最大次数
     **/
    Boolean judgeMaxTimesByUserId(String key, String min, String max);
}
