package com.allst.redis.anno;

import com.allst.redis.enums.LimitType;

import java.lang.annotation.*;

/**
 * 限流注解
 * @author Hutu
 * @since 2024-07-06 下午 02:41
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLimit {
    // 限流key
    String key() default "api:limit:";
    // 限流时间
    int time() default 60;
    // 限流次数
    int count() default 100;
    // 限流类型
    LimitType limitType() default LimitType.DEFAULT;
}
