# allst-graphql

岸口货栈是一个 Spring Boot 2.7 + GraphQL 的电商接口示例，用来固定本仓库里 GraphQL 的写法。后端 Java 11，数据放在 H2，柜台是 Vue 3。

能做的事：分类、商品、客户、地址、订单的增删改查；分页和筛选；一次查询带出关联；下单扣库存、取消回库存；订单状态订阅；自定义金额和日期标量。

## 运行

```bash
mvn spring-boot:run
```

GraphiQL：http://localhost:8089/graphiql

```bash
cd frontend
npm install
npm run dev
```

柜台：http://localhost:5173

## 文档

| 文档 | 内容 |
| --- | --- |
| [背景笔记](docs/00-背景笔记.md) | 原先的 GraphQL 学习摘录 |
| [目标与技术选型](docs/01-目标与技术选型.md) | 为什么用 Spring for GraphQL |
| [工程结构](docs/02-工程结构.md) | 包和分层 |
| [持久化建模](docs/03-持久化建模.md) | 表、快照、约束、样例数据 |
| [Schema 与类型](docs/04-Schema与类型规范.md) | View、Input、空值、校验 |
| [查询分页与过滤](docs/05-查询分页与过滤.md) | 分页和商品筛选 |
| [变更与订单流转](docs/06-变更校验与订单流转.md) | 下单、状态、删除条件 |
| [嵌套与 BatchMapping](docs/07-嵌套查询与BatchMapping.md) | 避免 N+1 |
| [错误、标量、查询保护](docs/08-错误处理自定义标量与查询保护.md) | code、DateTime、深度限制 |
| [订阅](docs/09-订阅.md) | 订单状态推送 |
| [Vue 3 柜台](docs/10-Vue3柜台.md) | 前端如何调用 |
| [运行与示例](docs/11-运行与请求示例.md) | 启动命令和可粘贴的请求 |
