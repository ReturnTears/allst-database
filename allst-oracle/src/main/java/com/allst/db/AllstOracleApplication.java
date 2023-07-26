package com.allst.db;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.allst.db.mapper")
public class AllstOracleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllstOracleApplication.class, args);
    }

}
