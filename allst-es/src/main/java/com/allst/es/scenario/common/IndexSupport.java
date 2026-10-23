package com.allst.es.scenario.common;

import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 场景索引由实体注解生成 mapping，不在启动时连接 Elasticsearch。
 * 第一次查询或显式初始化时再创建，避免 ES 不可用导致整个应用起不来。
 */
@Component
public class IndexSupport {
    private final ElasticsearchRestTemplate template;
    private final Set<Class<?>> ready = new HashSet<Class<?>>();

    public IndexSupport(ElasticsearchRestTemplate template) {
        this.template = template;
    }

    public synchronized boolean init(Class<?> entityClass) {
        IndexOperations operations = template.indexOps(entityClass);
        boolean created = false;
        if (!operations.exists()) {
            created = operations.createWithMapping();
        }
        ready.add(entityClass);
        return created;
    }

    public synchronized void ensure(Class<?> entityClass) {
        if (ready.contains(entityClass)) {
            return;
        }
        init(entityClass);
    }

    public void refresh(Class<?> entityClass) {
        template.indexOps(entityClass).refresh();
    }
}
