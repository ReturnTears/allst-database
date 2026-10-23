# 16. 自定义 HandlerMethodArgumentResolver

Spring MVC 可以把查询参数绑到一个普通对象上，但搜索条件还会混入类型转换和默认值。`EmployeeSearchArgumentResolver` 只处理带了 `@BindEmployeeSearch` 的参数，避免拦截所有复杂类型。

它从查询字符串读取：

- `name`、`departmentName`
- `minAge`、`maxAge`
- `minSalary`
- `status`，必须是 `EmployeeStatus` 的名字

空字符串当成 null。数字或枚举不合法时抛 `400`，不把转换异常留到仓库层。

注册位置是 `WebMvcGuideConfig.addArgumentResolvers`。`Pageable` 的解析器仍然由 Spring Data Web 提供，这里不重复注册。

控制器单元测试没有启动容器，用 `MockMvcBuilders.standaloneSetup` 把这个解析器和 `PageableHandlerMethodArgumentResolver` 装上，就能验证绑定结果。
