# allst-dynamic

Spring Boot 2.7.18 / Java 17 下的多数据源和动态数据源规范。旧的 City 练习保留在 `com.allst.dynamic.controller` 和 `doc/edit.sql`。

新场景的说明、接口和建库脚本在 [docs/datasource-scenarios/README.md](docs/datasource-scenarios/README.md)。

本地核对路由算法和整条调用链：

```bat
mvn test
```

集成测试使用 H2，不要求本机已经建好 MySQL 场景库。要用接口打到 MySQL 时，先执行 `docs/datasource-scenarios/sql/mysql-init.sql`，再启动 `DynamicApplication`。默认地址是 `http://127.0.0.1:8000/dynamic/scenario`。
