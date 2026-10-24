package com.allst.db.guide.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 显式 Neo4j 事务。未提交就关闭时会回滚。
 */
public final class Neo4jTransaction implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Neo4jTransaction.class);

    private final Neo4jHttpClient client;
    private final String id;
    private boolean finished;

    Neo4jTransaction(Neo4jHttpClient client, String id) {
        this.client = client;
        this.id = id;
    }

    public CypherRowSet run(String statement, Map<String, Object> parameters) {
        ensureOpen();
        return client.runIn(id, new CypherStatement(statement, parameters));
    }

    public void commit() {
        ensureOpen();
        client.commitOpen(id);
        finished = true;
    }

    public void rollback() {
        if (finished) {
            return;
        }
        try {
            client.rollback(id);
        } finally {
            finished = true;
        }
    }

    @Override
    public void close() {
        if (finished) {
            return;
        }
        try {
            rollback();
        } catch (RuntimeException ex) {
            log.warn("回滚 Neo4j 事务失败: {}", ex.getMessage());
        }
    }

    private void ensureOpen() {
        if (finished) {
            throw new IllegalStateException("事务已结束");
        }
    }

    public String getId() {
        return id;
    }
}
