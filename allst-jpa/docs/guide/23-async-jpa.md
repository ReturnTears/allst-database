# 23. 在 CompletableFuture 里使用 JPA

`@Async` 会把调用丢到 `jpaExecutor` 线程池。这条线程上没有请求线程的事务，也没有 open-in-view。

不要把 `@Async` 和 `@Transactional` 写在同一个方法上。异步拦截器拿到线程后直接调目标方法，同一个类上的事务代理经常被绕过，方法看起来有事务，实际没有。

正确拆法：

- `AsyncEmployeeService.loadDepartmentName` 只有 `@Async("jpaExecutor")`。
- 它调用另一个 Bean：`EmployeeLookupService.departmentName`。
- 后者是 `@Transactional(readOnly = true)`，在异步线程上新开持久化上下文，再访问懒加载部门。

线程池在 `GuideAsyncConfig`。应用关闭时会等待任务结束，避免异步里的事务被进程直接掐断。

调用方使用 `future.get(5, SECONDS)` 或继续链式处理。不要在持有外部事务时阻塞等待一个需要同一条连接的异步任务，那样会把自己卡死。
