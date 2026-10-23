package com.allst.boot.guide.web;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 只暴露连接池和持久化单元的类型信息，不返回用户名和密码。
 */
@RestController
@RequestMapping("/guide/infra")
public class InfraController {

    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private final boolean openInView;

    public InfraController(DataSource dataSource,
                           EntityManagerFactory entityManagerFactory,
                           @Value("${spring.jpa.open-in-view:true}") boolean openInView) {
        this.dataSource = dataSource;
        this.entityManagerFactory = entityManagerFactory;
        this.openInView = openInView;
    }

    @GetMapping("/datasource")
    public Map<String, Object> datasource() {
        Map<String, Object> info = new LinkedHashMap<>();
        DataSource target = dataSource;
        info.put("dataSourceClass", target.getClass().getName());
        if (target instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) target;
            info.put("poolName", hikari.getPoolName());
            info.put("maximumPoolSize", hikari.getMaximumPoolSize());
            info.put("minimumIdle", hikari.getMinimumIdle());
            info.put("jdbcUrl", hikari.getJdbcUrl());
        }
        info.put("entityManagerFactory", entityManagerFactory.getClass().getName());
        info.put("openInView", openInView);
        info.put("statisticsEnabled", entityManagerFactory.unwrap(org.hibernate.SessionFactory.class)
                .getStatistics().isStatisticsEnabled());
        return info;
    }
}
