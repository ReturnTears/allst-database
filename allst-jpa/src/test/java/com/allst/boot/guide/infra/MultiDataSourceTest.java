package com.allst.boot.guide.infra;

import com.allst.boot.guide.entity.Department;
import com.allst.boot.guide.secondary.ReportRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MultiDataSourceTest {

    @Test
    void primaryAndSecondaryKeepSeparateRows() {
        HikariDataSource primaryDataSource = PersistenceUnitFactory.h2("multiPrimary", 3);
        HikariDataSource secondaryDataSource = PersistenceUnitFactory.h2("multiSecondary", 3);
        LocalContainerEntityManagerFactoryBean primaryFactory = PersistenceUnitFactory.create(
                primaryDataSource, "primary", "com.allst.boot.guide.entity");
        LocalContainerEntityManagerFactoryBean secondaryFactory = PersistenceUnitFactory.create(
                secondaryDataSource, "secondary", "com.allst.boot.guide.secondary");
        primaryFactory.afterPropertiesSet();
        secondaryFactory.afterPropertiesSet();
        EntityManagerFactory primary = primaryFactory.getObject();
        EntityManagerFactory secondary = secondaryFactory.getObject();
        try {
            EntityManager primaryManager = primary.createEntityManager();
            primaryManager.getTransaction().begin();
            Department department = new Department();
            department.setName("primary-only");
            primaryManager.persist(department);
            primaryManager.getTransaction().commit();
            primaryManager.close();

            EntityManager secondaryManager = secondary.createEntityManager();
            secondaryManager.getTransaction().begin();
            ReportRecord record = new ReportRecord();
            record.setTitle("daily");
            record.setBody("ok");
            secondaryManager.persist(record);
            secondaryManager.getTransaction().commit();
            Long recordId = record.getId();
            secondaryManager.close();

            EntityManager check = secondary.createEntityManager();
            ReportRecord loaded = check.find(ReportRecord.class, recordId);
            check.close();
            assertNotNull(loaded);
            assertEquals("daily", loaded.getTitle());
            assertEquals("multiPrimary", primaryDataSource.getPoolName());
            assertEquals(3, primaryDataSource.getMaximumPoolSize());
        } finally {
            primary.close();
            secondary.close();
            primaryDataSource.close();
            secondaryDataSource.close();
        }
    }
}
