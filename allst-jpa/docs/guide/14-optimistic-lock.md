# 14. 乐观锁与重试

`Account.version` 使用 `@Version`。两次事务读到同一个版本，后提交的那次会在 flush 时失败，异常是 `ObjectOptimisticLockingFailureException` 或 Hibernate 的 `StaleObjectStateException`。

重试必须包在事务外面。如果重试和事务在同一个方法里，失败的那次已经把事务标成 rollback-only，再试也会失败。

`AccountService.withdraw` 的结构：

1. `@Retryable` 最多 5 次，间隔 20 毫秒。
2. 每次尝试用 `TransactionTemplate` 开 `REQUIRES_NEW`。
3. 重新读取账户，按最新余额扣减，`saveAndFlush` 立刻触发版本检查。

并发测试在 `OptimisticLockRetryTest`：100 元同时扣 10 和 20，最终是 70。顺序执行时不会冲突，结果仍然是 70；真发生冲突时重试会读到对方提交后的版本。

员工实体也有 `@Version`，用来保护普通更新。批量 `@Modifying` 不会增加这个版本号，需要的话要在 JPQL 里自己写 `version = version + 1`，并带上旧版本条件。
