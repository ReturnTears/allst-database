# 25. N+1

`CustomerOrder.items` 是懒加载。`OrderQueryService.countItemsNaive` 先 `findAll`，再循环 `getItems()`。没有批量时这是 1 条订单 SQL 加 N 条明细 SQL。

实体上的 `@BatchSize(size = 2)` 把 N 收成若干条 `IN` 查询。6 张订单大约是 1 + 3 条，仍然多于一次抓取。

一次抓取有两种：

- `findAllWithItems`：`left join fetch`
- `findAllWithGraph`：`@EntityGraph("Order.withItems")`

`left join fetch` 用左连接，没有明细的订单也会回来。普通 `join fetch` 是内连接，空订单会被丢掉。集合查询加上 `distinct`，避免一单一行明细变成多个订单对象。

分页不能和 `join fetch` 写在一起。`pageWithItems` 先 `pageIds`，再 `findWithItemsByIdIn`。

测试打开了 Hibernate statistics，比较 naive 和 fetch join 的 `prepareStatementCount`。fetch join 的语句数更少，明细条数相同。
