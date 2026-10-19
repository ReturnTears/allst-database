package com.allst.dynamic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

/**
 * @author Hutu
 * @since 2024-11-20 下午 09:42
 */
@SpringBootApplication
@MapperScan("com.allst.dynamic.mapper")
public class DynamicApplication {
    public static void main(String[] args) {
        StopWatch watch = new StopWatch();
        watch.start();
        SpringApplication.run(DynamicApplication.class, args);
        watch.stop();
        System.out.println("-----startup : -----" + watch.getTotalTimeMillis());
    }
}