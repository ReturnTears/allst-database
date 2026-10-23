package com.allst.boot.guide.event;

import java.time.LocalDateTime;

/**
 * 实体回调只负责发布事件。真正写审计表放到事务提交之后，避免在 flush 过程中再插入另一张表。
 */
public class EntityAuditEvent {

    private final String entityName;
    private final String action;
    private final LocalDateTime happenedAt;

    public EntityAuditEvent(String entityName, String action, LocalDateTime happenedAt) {
        this.entityName = entityName;
        this.action = action;
        this.happenedAt = happenedAt;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getHappenedAt() {
        return happenedAt;
    }
}
