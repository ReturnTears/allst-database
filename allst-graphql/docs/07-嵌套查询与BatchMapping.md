# 07 嵌套查询与 BatchMapping

客户端可以在一次请求里跨聚合取数：

```graphql
{
  orders(page: { page: 0, size: 8 }) {
    content {
      orderNo
      totalAmount
      customer { name }
      items { productName quantity amount }
    }
  }
}
```

如果订单列表有 N 条，而每条订单的客户都单独查一次，就是 N+1。这里的关联字段注册了 `@BatchMapping`：框架先把本层对象收集起来，再调用一次批量方法。

| Schema 字段 | 注册位置 | 批量方法 |
| --- | --- | --- |
| `Product.category` | `CatalogGraphqlController` | `CatalogService.categoriesOf` |
| `Category.parent` / `children` / `productCount` | 同上 | `parentsOf` / `childrenOf` / `productCounts` |
| `Customer.addresses` | `CustomerGraphqlController` | `addressesOf` |
| `Customer.orders` | `OrderGraphqlController` | `ordersOf` |
| `Order.customer` | 同上 | 按客户 id 一次 `findAllById` |
| `OrderItem.product` | 同上 | `productsOf` |

批量方法返回 `Map`，key 必须是参数列表里的那个对象实例。View 没有重写 `equals`，按对象身份对应。

返回 Map 里没有的 key，GraphQL 会把该字段当成 null。`Category.parent` 和 `OrderItem.product` 在 Schema 里可空，所以允许缺 key。`Product.category` 非空，找不到分类时直接抛错。

订单明细几乎每次都会和订单一起读，但它是一对多。分页查询如果 `join fetch` 集合，Hibernate 可能把分页做乱，也可能出现重复行。因此 `OrderService.toViews` 先查订单页，再按这页的订单 id 一次查出明细，在内存里分组。这是另一种批量，不是 `@BatchMapping`。

`inStock` 和 `itemCount` 不访问数据库，留在 View 的 getter 上，不参与批量加载。

查询深度在 `GraphqlRuntimeConfig` 里限制为 15，用来挡住 `category { children { children { ... } } }` 这种递归。复杂度上限是 5000。GraphiQL 打开时会发很宽的内省查询，上限如果压到几百，控制台就打不开。生产环境应按自己的查询收紧，并单独放行内省。
