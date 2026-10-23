package com.allst.boot.guide.infra;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示数据源从属性到 Hikari，再到 EntityManagerFactory 的组装过程。
 * Spring Boot 自动配置做的是同一条链路，只是把这些步骤拆进了 DataSourceAutoConfiguration 和 HibernateJpaAutoConfiguration。
 */
public final class PersistenceUnitFactory {

    private PersistenceUnitFactory() {
    }

    public static HikariDataSource h2(String name, int maximumPoolSize) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName(name);
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:" + name + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(1);
        return dataSource;
    }

    public static LocalContainerEntityManagerFactoryBean create(DataSource dataSource, String unitName, String... packages) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(packages);
        factory.setPersistenceUnitName(unitName);
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        factory.setJpaVendorAdapter(adapter);
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        factory.setJpaPropertyMap(properties);
        return factory;
    }
}
