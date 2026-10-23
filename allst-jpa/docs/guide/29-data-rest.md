# 29. Spring Data REST 与 JPA

`spring-boot-starter-data-rest` 会把仓库导出成 HTTP 资源。检测策略设成 `annotated`，只有写了 `@RepositoryRestResource` 的接口才会暴露。因此旧的 `UsersRepository` 不会出现在 `/api` 下。

`DepartmentRepository` 导出到 `/api/departments`。POST JSON 可以创建部门，创建之后会走到 `GuideRepositoryEventListener`。

Data REST 适合管理型、资源和仓库一一对应的接口。员工搜索、扣款、分表这些有业务规则的操作仍由自己的控制器完成。

导出实体时注意：

- 懒加载集合会被序列化碰到。部门和员工用了 `@JsonManagedReference` / `@JsonBackReference`。
- 密码这类字段即使走 REST 也必须 `WRITE_ONLY` 或不要导出那个仓库。
- 关联资源默认还能继续链出去。不希望外部直接改关联时，用 `@RestResource(exported = false)` 关掉对应仓库。
