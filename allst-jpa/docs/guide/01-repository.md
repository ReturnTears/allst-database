# 1. Repository 与自定义 Repository

`Repository` 是标记接口，本身没有方法。Spring Data 只给声明出来的方法生成实现。旧代码里的 `MyBaseRepository` 就是这种窄接口：只有 `findById` 和 `save`。

指南场景统一继承 `GuideBaseRepository`。它再继承 `JpaRepository`，因此同时具备：

- CRUD
- 分页排序
- `QueryByExampleExecutor`（Example 查询不用再单独继承）

`EmployeeRepository` 在此之上再组合三样东西：

- `JpaSpecificationExecutor`：动态条件
- `QuerydslPredicateExecutor`：类型安全查询
- `EmployeeRepositoryCustom`：Criteria API 写不进方法名的搜索

接口上的 `default` 方法不会生成 SQL，只是把已有查询再过滤一次。`findActiveByEmail` 就是这样。

扫描入口在 `RepositoryConfiguration`。JPA 只扫 `com.allst.boot.repository.jpa` 和 `com.allst.boot.guide.repository`，Mongo 仍然只扫原来的包。
