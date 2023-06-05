package com.allst.redis.aspect;

import com.allst.redis.pojo.User;
import com.allst.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Hutu
 * @since 2023-06-05 下午 11:18
 */
@Aspect
@Component
@Slf4j
@Order(1)
public class CheckVisitTimesAroundAspection {

    @Autowired
    private RedisService redisService;

    /**
     * 设置切点
     */
    @Pointcut("@annotation(com.allst.redis.anno.CheckVisitTimesAroundAnnotation) ")
    public void entryPoint() {
    }

    /**
     * 前置处理
     */
    @Before(value = "entryPoint()")
    public void doBefore(JoinPoint joinPoint) {
        log.info("前置处理请求参数：{}", joinPoint);
    }

    /**
     * 后置处理
     */
    @AfterReturning(pointcut = "entryPoint()", returning = "user")
    public void doAfterReturning(JoinPoint joinPoint, User user) {

        log.info("后置处理请求参数：{}", joinPoint);

    }

    /**
     * 异常处理
     */
    @AfterThrowing(pointcut = "entryPoint()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, Exception exception) {

        log.info("异常处理请求参数：{}", joinPoint);

    }

    /**
     * 前置切面
     */
    @Around("entryPoint()")
    public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint) {
        log.info("------检查userId访问次数限制------start");
        Object[] args = proceedingJoinPoint.getArgs();
        System.out.println(args);
        //打印入口方法以及入参
        try {
            for (Object arg : args) {
                Boolean flag = judgeVisitTimesByUserId(arg);
                if (!flag) {
                    // 返回异常对象
                    return new Object();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("不能打印请求参数");
        }
        //执行请求方法
        Object o = null;
        try {
            o = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        log.info("------检查userId访问次数限制------end");
        return o;

    }

    // 检查userId请求接口限制
    public Boolean judgeVisitTimesByUserId(Object object) {
        User user = (User) object;
        String userId = StringUtils.isEmpty(user.getUserId()) ? "101" : user.getUserId();
        if (!StringUtils.isEmpty(userId)) {
            return redisService.judgeMaxTimesByUserId(userId, "1", "10");
        }
        return true;
    }
}
