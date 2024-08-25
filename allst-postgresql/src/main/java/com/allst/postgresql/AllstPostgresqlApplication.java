package com.allst.postgresql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AllstPostgresqlApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllstPostgresqlApplication.class, args);
        System.out.println("PostgreSQL启动成功");
    }
}
