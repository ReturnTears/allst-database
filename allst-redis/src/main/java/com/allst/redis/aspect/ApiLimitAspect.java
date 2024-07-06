package com.allst.redis.aspect;

import com.allst.redis.anno.ApiLimit;
import com.allst.redis.enums.LimitType;
import com.allst.redis.utils.IPUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * @author Hutu
 * @since 2024-07-06 下午 02:56
 */
@Slf4j
@Aspect
@Component
public class ApiLimitAspect {
    private final RedisScript<Long> redisScript;
    private final RedisTemplate<Object, Object> redisTemplate;

    public ApiLimitAspect(RedisScript<Long> redisScript, RedisTemplate<Object, Object> redisTemplate) {
        this.redisScript = redisScript;
        this.redisTemplate = redisTemplate;
    }

    @Before(value = "@annotation(apiLimit)")
    public void before(JoinPoint joinPoint, ApiLimit apiLimit) {
        String key = apiLimit.key();
        int time = apiLimit.time();
        int count = apiLimit.count();
        // api:limit:localhost-com.allst.redis.controller.ApiLimitController-apiLimit
        String combineKey = getCombineKey(joinPoint, apiLimit);
        List<Object> keys = Collections.singletonList(combineKey);
        Long execute = redisTemplate.execute(redisScript, keys, count, time);
        System.out.println("execute:" + execute);
        if (execute == null || execute.intValue() > count) {
            throw new RuntimeException("访问频繁， 稍后重试");
        }
    }

    private String getCombineKey(JoinPoint joinPoint, ApiLimit apiLimit) {
        StringBuilder stringBuilder = new StringBuilder(apiLimit.key());
        if (apiLimit.limitType() == LimitType.IP) {
            //String ipAddr = IPUtils.getIpAddr(((HttpServletRequest) RequestContextHolder.currentRequestAttributes()));
            String ipAddr = IPUtils.getIpDefault();
            stringBuilder.append(ipAddr).append("-");
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuilder.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuilder.toString();
    }
}
