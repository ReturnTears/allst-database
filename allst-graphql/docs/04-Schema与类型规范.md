# 04 Schema 与类型规范

Schema 放在 `src/main/resources/graphql/`。Spring 会合并该目录下全部 `.graphqls`。

- `root.graphqls` 定义标量、枚举、`PageInput`，以及 `type Query` 的 `health`
- 其他文件用 `extend type Query` / `extend type Mutation` 追加字段
- `customer.graphqls` 首先声明 `type Mutation`，后面的文件才能 extend
- `order.graphqls` 声明 `type Subscription`

## 三类类型不要混用

| GraphQL | Java | 作用 |
| --- | --- | --- |
| `Product` | `ProductView` | 返回给客户端 |
| `ProductInput` | `ProductInput` | 创建和全量更新 |
| 实体 `Product` | 只在 Service 和 Repository 内部使用 | 持久化 |

View 上可以有 Schema 里没有的字段，例如 `ProductView.categoryId`。GraphQL 只读取 Schema 声明过的属性。关联对象不要做成 View 的 getter，否则会绕过 `@BatchMapping`。`inStock`、`itemCount` 这种由已有数据算出的值，直接写在 View 的 getter 上。

`interface Node { id: ID! }` 用来约定标识字段。当前没有哪个字段的返回类型是 `Node` 本身，但 GraphQL Java 仍然要求接口具备 TypeResolver，注册位置在 `GraphqlRuntimeConfig`。

## 空与非空

按 id 查询的字段（`product`、`customer`、`order`）在 Schema 里是非空的。找不到就抛 `NOT_FOUND`，响应里 `data` 为 null，并带 `errors`。列表查询找不到数据时返回空页或空数组，不报错。

## 校验分两层

1. Schema 的 `!` 负责“这个参数必须出现”。缺参数时，错误来自 GraphQL 校验，没有业务 code。
2. `InputValidator` 使用 Bean Validation，负责格式：邮箱、金额至少 `0.01`、最多两位小数、库存不能为负。失败 code 是 `VALIDATION`。

金额在变量里传字符串，例如 `"128.00"`。自定义标量 `BigDecimal` 也接受数字，但浮点会丢失精度，客户端不要传 JSON number。
