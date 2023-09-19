package com.allst.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hutu
 * @since 2023-09-19 下午 08:33
 */
@SpringBootApplication
public class AllstSolrApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        var application = new SpringApplication(AllstSolrApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}