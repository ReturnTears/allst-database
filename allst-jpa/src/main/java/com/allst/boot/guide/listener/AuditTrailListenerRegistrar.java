package com.allst.boot.guide.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 在容器启动时把事件发布器交给 JPA 回调。回调类本身仍然由 Hibernate 实例化。
 */
@Component
public class AuditTrailListenerRegistrar {

    public AuditTrailListenerRegistrar(ApplicationEventPublisher publisher) {
        AuditTrailListener.bind(publisher);
    }
}
