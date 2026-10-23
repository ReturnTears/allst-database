package com.allst.boot.guide.sharding;

import com.allst.boot.guide.infra.PersistenceUnitFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShardingSphereJpaTest {

    @Test
    void jpaPersistsIntoModuloTables() throws Exception {
        HikariDataSource raw = ShardingSphereExampleFactory.createRawDataSource();
        DataSource sharding = ShardingSphereExampleFactory.createShardingDataSource(raw);
        LocalContainerEntityManagerFactoryBean factory = PersistenceUnitFactory.create(
                sharding, "sharding", "com.allst.boot.guide.sharding");
        factory.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "none");
        factory.afterPropertiesSet();
        EntityManagerFactory entityManagerFactory = factory.getObject();
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist(order(1L, 1L, "10.00"));
            entityManager.persist(order(2L, 2L, "20.00"));
            entityManager.getTransaction().commit();
            entityManager.close();

            EntityManager queryManager = entityManagerFactory.createEntityManager();
            List<ShardingOrder> orders = queryManager
                    .createQuery("select o from ShardingOrder o", ShardingOrder.class)
                    .getResultList();
            queryManager.close();
            assertEquals(2, orders.size());

            try (Connection connection = raw.getConnection();
                 Statement statement = connection.createStatement()) {
                assertEquals(1, count(statement, "g_shard_order_0"));
                assertEquals(1, count(statement, "g_shard_order_1"));
            }
        } finally {
            entityManagerFactory.close();
            raw.close();
        }
    }

    private ShardingOrder order(Long id, Long userId, String amount) {
        ShardingOrder order = new ShardingOrder();
        order.setId(id);
        order.setUserId(userId);
        order.setAmount(new BigDecimal(amount));
        return order;
    }

    private int count(Statement statement, String table) throws Exception {
        try (ResultSet resultSet = statement.executeQuery("select count(*) from " + table)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
