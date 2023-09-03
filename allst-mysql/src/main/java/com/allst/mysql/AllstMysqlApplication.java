package com.allst.mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class AllstMysqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllstMysqlApplication.class, args);
    }

}
