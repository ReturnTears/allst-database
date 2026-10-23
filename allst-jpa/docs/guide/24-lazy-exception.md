# 24. LazyInitializationException 怎么处理

异常的含义是：实体已经脱离持久化上下文，代理后面没有 Session。它不是关联映射写错了。

处理顺序：

1. 在还开着的事务里把要用的关联取出来。`EmployeeLookupService` 在事务方法内读 `department.getName()`。
2. 查询时一次抓取。`@EntityGraph("Employee.details")` 或 `join fetch`。
3. 只返回 DTO / 投影，表现层碰不到代理。员工列表走 `EmployeeSummary`。
4. 集合上的 `@BatchSize` 只能减少加载次数，访问时仍然需要 Session。
5. Jackson 注册 `Hibernate5Module` 且不要强制懒加载，避免序列化时才发现没有 Session。
6. open-in-view 能让异常消失，但把 SQL 推迟到视图阶段。只把它当作兼容手段，见 [22](22-open-in-view.md)。

`GuideJpaBehaviorTest` 在 `TransactionTemplate` 提交后访问部门，断言抛出这个异常。这是没有 Web 请求时的正常结果。
