# 22. open-in-view 对事务的影响

`spring.jpa.open-in-view=true` 是 Boot 的默认值，本项目把它写明了。请求进入时 `OpenEntityManagerInViewInterceptor` 打开 EntityManager，请求结束才关闭。期间如果有事务，事务使用这同一个上下文；事务提交后上下文仍然开着，所以控制器还能触发懒加载。

这会带来两个后果：

- 事务已经提交，控制器里的懒加载仍会再向池借连接。连接占用时间变成整个请求，而不再是事务本身。
- 视图或序列化里的意外访问会在请求末尾发出 SQL，N+1 更难被发现。

`EmployeeController.departmentName` 就是这个行为：服务方法返回后再读部门名。集成测试在默认配置下得到部门名。`OpenInViewDisabledTest` 把属性改成 `false`，同一次访问抛出 `LazyInitializationException`。

接口应在事务内把需要的数据装进 DTO 或用 fetch join / EntityGraph 取完。open-in-view 可以在确认没有视图依赖懒加载之后关掉。
