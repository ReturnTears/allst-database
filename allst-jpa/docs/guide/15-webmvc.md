# 15. JPA 对 WebMVC 的支持

Spring Boot 会启用 Spring Data Web，控制器可以直接声明：

- `Pageable`：`page`、`size`、`sort`
- `Sort`：`sort=empName,desc` 这种参数。属性名是 Java 属性，不是列名

`EmployeeController.page` 用 `@PageableDefault(size = 10, sort = "id")` 给出默认值。全局上限在：

```properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=200
```

域名转换（`/employees/1` 直接得到实体）也能开，但控制器会依赖存储。本项目把 id 留在路径上，由服务查询，失败时抛 `NotFoundException`。这个异常带了 `@ResponseStatus(NOT_FOUND)`。

`/guide/employees/{id}/department-name` 在服务方法返回后继续访问懒加载部门。open-in-view 打开时请求还能查到，关掉就失败。这是 Web 层和持久化上下文绑在一起的例子，见 [22](22-open-in-view.md)。
