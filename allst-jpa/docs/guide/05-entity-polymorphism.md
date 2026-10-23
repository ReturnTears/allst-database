# 5. `@Entity` 常用注解与多态

公共审计字段放在 `@MappedSuperclass` 的 `BaseAuditableEntity` 上，子类表会带上这些列，但超类本身没有表。

员工、部门、客户这些具体类型使用：

- `@Entity`、`@Table`、`@Index`
- `@Id` + `GenerationType.IDENTITY`
- `@Column` 控制可空、长度、精度和个别物理列名
- `@Enumerated(EnumType.STRING)`
- `@Version`
- `@Transient` 表示不入库，例如 `Customer.loaded`

多态用 JOINED 继承，见 `Product`、`Book`、`DigitalGoods`：

- 父表 `g_product` 放名称和价格，并用 `product_type` 区分子类。
- 子表只放自己的列，主键同时是指向父表的外键。
- 仓库声明成 `JpaRepository<Product, Long>` 时，`findAll` 会把 `Book` 和 `DigitalGoods` 一起查回来。
- 只查书时写 `select b from Book b`。

Jackson 侧用 `@JsonTypeInfo` 输出 `kind`，这样接口能看出运行时类型。这是序列化多态，和 JPA 的 `@DiscriminatorValue` 各管一层。

不要在实体上用 Lombok `@Data`。它会把懒加载关联算进 `equals` / `hashCode`，集合一比较就触发加载。
