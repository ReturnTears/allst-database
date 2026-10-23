package com.allst.boot.guide.service;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 查询计划缓存命中的是解析后的 SQL 计划，不是查询结果。
 * 相同 JPQL、不同参数应命中同一份计划。IN 子句参数个数变化会拆成多份计划，所以打开了参数补齐。
 */
@Service
public class QueryPlanCacheService {

    @PersistenceContext
    private EntityManager entityManager;

    public Statistics statistics() {
        return entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    }

    public long hitCount() {
        return statistics().getQueryPlanCacheHitCount();
    }

    public long missCount() {
        return statistics().getQueryPlanCacheMissCount();
    }
}
