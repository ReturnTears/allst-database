package com.allst.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hutu
 * @since 2024-08-13 下午 09:43
 */
@SpringBootApplication
public class PrestoApplication {
    public static void main(String... args) {
        System.out.println("Hello world!");
        SpringApplication.run(PrestoApplication.class, args);
    }
}