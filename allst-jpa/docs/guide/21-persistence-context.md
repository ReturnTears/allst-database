# 21. Persistence Context

持久化上下文是一组被 EntityManager 跟踪的实体，不是数据库连接本身。Spring 的默认范围是当前事务：事务开始时绑定，提交或回滚时关闭。

`PersistenceContextService` 演示三件事：

- `sameInstance`：同一个事务里两次 `find` 同一个 id，得到的是同一个 Java 对象。
- `detachedAfterClear`：`clear()` 之后实体不再被跟踪，再改字段也不会写回数据库。
- 事务方法返回后，如果没有 open-in-view，实体就是 detached。

修改被跟踪的字段不需要再调用 `save`。事务提交时脏检查会生成 UPDATE。`save` 对已存在的实体实际是 `merge`。对新建对象，`save` 才会 `persist`。

`refresh` 用数据库当前行覆盖内存对象，适合原生 SQL 已经改过行、上下文里还是旧值的情况。`GuideJpaBehaviorTest` 里有这个过程。

一个事务里不要同时使用两个没有说明过的 EntityManager。多数据源必须把仓库绑到对应的持久化单元，否则两边会共用一个上下文。
