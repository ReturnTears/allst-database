package com.allst.dynamic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hutu
 * @since 2024-11-20 下午 09:42
 */
@SpringBootApplication
@MapperScan("com.allst.dynamic.mapper")
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(Main.class, args);
    }
}