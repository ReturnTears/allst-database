# 34. 事务传播

`TransactionPropagationService.outerFailsAfterInnerCommit` 是一个事务。它调用另一个 Bean `TransactionPropagationWorker.saveInNewTransaction`，后者是 `REQUIRES_NEW`。

执行顺序：

1. 部门已经在外层事务开始前提交。内层是另一条连接，看不见外层还没提交的行。
2. 内层保存 `kept` 员工并提交。
3. 外层保存 `rolled` 员工后抛异常，外层回滚。
4. `kept` 还在，`rolled` 不在。

同一个类里自己调用 `this.saveInNewTransaction` 不会生效，因为调用不经过代理。所以内层方法放在独立的 Bean 上。

`REQUIRES_NEW` 会再向连接池借一条连接。外层事务还没结束时，池里至少占用两条。池子很小又嵌套很多新事务时，会在这里耗尽连接。

乐观锁重试使用同样的传播：每一次尝试都是新事务，失败只回滚这一次，下一次能读到已经提交的版本。
