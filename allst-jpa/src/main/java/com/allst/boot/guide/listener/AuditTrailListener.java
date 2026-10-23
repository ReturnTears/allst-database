package com.allst.boot.guide.listener;

import com.allst.boot.guide.entity.Customer;
import com.allst.boot.guide.event.EntityAuditEvent;
import org.springframework.context.ApplicationEventPublisher;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

/**
 * JPA 回调由 Hibernate 创建，不保证是 Spring Bean。
 * 通过 {@link #bind(ApplicationEventPublisher)} 把发布器交进来，回调里只改当前实体或发布事件。
 */
public class AuditTrailListener {

    private static ApplicationEventPublisher publisher;

    public static void bind(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    @PrePersist
    public void prePersist(Customer customer) {
        if (customer.getNickname() == null || customer.getNickname().isBlank()) {
            customer.setNickname(customer.getName());
        }
    }

    @PostPersist
    public void postPersist(Customer customer) {
        publish(customer, "PERSIST");
    }

    @PreUpdate
    public void preUpdate(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("客户姓名不能为空");
        }
    }

    @PostUpdate
    public void postUpdate(Customer customer) {
        publish(customer, "UPDATE");
    }

    @PreRemove
    public void preRemove(Customer customer) {
        publish(customer, "REMOVE");
    }

    @PostRemove
    public void postRemove(Customer customer) {
        customer.setLoaded(false);
    }

    @PostLoad
    public void postLoad(Customer customer) {
        customer.setLoaded(true);
    }

    private void publish(Customer customer, String action) {
        if (publisher == null) {
            return;
        }
        publisher.publishEvent(new EntityAuditEvent(
                customer.getClass().getSimpleName(),
                action,
                LocalDateTime.now()
        ));
    }
}
