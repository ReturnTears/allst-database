package com.allst.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hutu
 * @since 2024-07-31 下午 10:09
 */
@SpringBootApplication
public class SpringDataJpaApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(SpringDataJpaApplication.class, args);
    }
}