# 7. Jackson 在实体上的注解

客户接口直接返回 `Customer`，用来对照这些注解。员工列表不这么做，列表走 DTO。

| 注解 | 作用 | 位置 |
| --- | --- | --- |
| `@JsonIgnoreProperties` | 去掉 Hibernate 代理上的 `handler` | 多个实体 |
| `@JsonInclude(NON_NULL)` | 空字段不输出 | `Customer` |
| `@JsonProperty("customer_name")` | 改 JSON 字段名 | 姓名 |
| `@JsonProperty(access = WRITE_ONLY)` | 密码只能写入 | `password` |
| `@JsonFormat` | 生日格式 `yyyy-MM-dd` | `birthday` |
| `@JsonView` | 公开接口不带手机号，内部接口带 | `Views` |
| `@JsonManagedReference` / `@JsonBackReference` | 打断部门和员工的循环引用 | 关联两端 |
| `@JsonTypeInfo` / `@JsonSubTypes` | 多态商品输出 `kind` | `Product` |

`JacksonGuideConfig` 注册了 `Hibernate5Module`，并且关闭了强制懒加载。未初始化的代理序列化成 null，不会在写 JSON 时偷偷查库，也不会因为没有 Session 把整个请求打成 500。

视图要加在控制器方法上：`CustomerController` 的公开方法和 `/internal` 使用了不同的 `@JsonView`。字段上没有视图、方法上却指定了视图时，该字段不会出现。
