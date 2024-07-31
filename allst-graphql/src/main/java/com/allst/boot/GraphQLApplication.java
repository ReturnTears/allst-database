package com.allst.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hutu
 * @since 2024-06-28 下午 10:26
 */
@SpringBootApplication
public class GraphQLApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(GraphQLApplication.class, args);
    }
}