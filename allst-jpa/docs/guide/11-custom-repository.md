# 11. 如何自定义 Repository

这里有两层，不要混成一个类。

## 基类

`GuideBaseRepository` 声明所有仓库都要的 `refresh`。`GuideBaseRepositoryImpl` 继承 `SimpleJpaRepository`，构造器必须是：

```text
(JpaEntityInformation<T, ?>, EntityManager)
```

`@EnableJpaRepositories(repositoryBaseClass = GuideBaseRepositoryImpl.class)` 之后，旧的 `UsersRepository` 和新的指南仓库都用这个基类。旧接口没有 `refresh` 方法，代理就不会把这个方法暴露出去，原有调用不受影响。

## 片段

某个仓库才需要的动态 SQL 做成片段：

- 接口：`EmployeeRepositoryCustom`
- 实现：`EmployeeRepositoryCustomImpl`

Spring Data 2.7 按“片段接口名 + Impl”找实现类，并且实现类要和接口在同一个包。实现类不要标 `@Component`，仓库工厂会自己创建它。`@PersistenceContext` 能注入到这个实例上。

`refresh` 只能用于当前持久化上下文里的实体。对已经脱离的对象调用会失败。
