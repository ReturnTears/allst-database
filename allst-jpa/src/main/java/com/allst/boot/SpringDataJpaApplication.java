package com.allst.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * @author Hutu
 * @since 2024-07-31 下午 10:09
 */
@SpringBootApplication
@EntityScan(basePackages = {
        "com.allst.boot.entity",
        "com.allst.boot.guide.entity"
})
public class SpringDataJpaApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(SpringDataJpaApplication.class, args);
    }
}