# 31. EsRepository 和 JpaRepository 同时存在

两个模型不要合成一个类。

- `Employee` 是 JPA 实体，仓库是 `EmployeeRepository`。
- `EmployeeDocument` 只有 Elasticsearch 注解，仓库是 `EmployeeSearchRepository`。
- `EmployeeDocument.from` 在保存成功后复制需要搜索的字段。

正式启动和测试默认排除了 Elasticsearch 的自动配置。否则仓库初始化会连 `localhost:9200`，没有服务时整个应用起不来。

打开方式：

```text
--spring.profiles.active=elasticsearch
```

`ElasticsearchGuideConfig` 创建 `RestHighLevelClient` 和 `ElasticsearchRestTemplate`，并只扫描 `com.allst.boot.guide.es`。`EmployeeIndexService.saveAndIndex` 先提交 JPA，再写索引。

索引写失败不会自动回滚数据库，除非再加事务消息或补偿任务。当前实现表达的是“两个仓库同时存在，由应用负责同步”，不是分布式事务。
