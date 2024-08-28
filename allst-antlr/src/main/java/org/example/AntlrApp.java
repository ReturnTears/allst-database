package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hutu
 * @since 2024-08-28 下午 10:17
 */
@SpringBootApplication
public class AntlrApp {
    public static void main(String[] args) {
        System.out.println("Hello Antlr!");
        SpringApplication.run(AntlrApp.class, args);
    }
}