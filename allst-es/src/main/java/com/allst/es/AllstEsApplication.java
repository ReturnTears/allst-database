package com.allst.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AllstEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllstEsApplication.class, args);
        System.out.println("Spring Elasticsearch Application Started");
    }
}
