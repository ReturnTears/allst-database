package com.allst.boot.guide.listener;

import com.allst.boot.guide.entity.AuditTrail;
import com.allst.boot.guide.event.EntityAuditEvent;
import com.allst.boot.guide.repository.AuditTrailRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 提交成功后再开一个新事务写审计表。
 * 监听器由事件基础设施直接调用，用 TransactionTemplate 保证一定有事务，不依赖代理嵌套。
 */
@Component
public class AuditTrailRecorder {

    private final AuditTrailRepository auditTrailRepository;
    private final TransactionTemplate transactionTemplate;

    public AuditTrailRecorder(AuditTrailRepository auditTrailRepository,
                              PlatformTransactionManager transactionManager) {
        this.auditTrailRepository = auditTrailRepository;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate = template;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void record(EntityAuditEvent event) {
        transactionTemplate.executeWithoutResult(status -> {
            AuditTrail trail = new AuditTrail();
            trail.setEntityName(event.getEntityName());
            trail.setAction(event.getAction());
            trail.setHappenedAt(event.getHappenedAt());
            auditTrailRepository.save(trail);
        });
    }
}
