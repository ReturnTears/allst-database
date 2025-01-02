package com.allst.es;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.allst.es.mapper")
public class AllstEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllstEsApplication.class, args);
        System.out.println("Spring Elasticsearch Application Started");
    }
}
