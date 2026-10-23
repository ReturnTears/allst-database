# 13. 实体回调与自定义 Listener

`Customer` 挂了 `AuditTrailListener`。回调由 Hibernate 创建，不能指望它是 Spring Bean，所以 `AuditTrailListenerRegistrar` 在启动时把 `ApplicationEventPublisher` 交进去。

回调里只做两类事：

- 改当前实体。`@PrePersist` 在昵称为空时用姓名补上。`@PreUpdate` 拒绝空姓名。`@PostLoad` 把临时标记 `loaded` 设为 true。
- 发布事件。`@PostPersist`、`@PostUpdate`、`@PreRemove` 发布 `EntityAuditEvent`。

不要在 flush 过程中再 `persist` 另一张表。`AuditTrailRecorder` 使用 `@TransactionalEventListener(AFTER_COMMIT)`，等业务事务提交成功后再用 `REQUIRES_NEW` 写 `g_audit_trail`。业务事务回滚时不会留下审计行。

另一类监听器是 Spring Data REST 的 `GuideRepositoryEventListener`。它只在 `/api` 导出接口创建、保存、删除 `Department` 时触发。普通 `repository.save` 不会走这里。
