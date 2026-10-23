package com.allst.boot.guide.repository.support;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;

/**
 * 替换 Spring Data 默认的 {@link SimpleJpaRepository}。
 * 构造器签名必须保持这两个参数，仓库工厂会按这个签名反射创建。
 */
public class GuideBaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements GuideBaseRepository<T, ID> {

    private final EntityManager entityManager;

    public GuideBaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public void refresh(T entity) {
        entityManager.refresh(entity);
    }
}
