# Elasticsearch 业务场景规范

这套规范用来约定 Spring Boot 项目里怎么使用 Elasticsearch。原有练习代码（`video`、`es_user`、binlog 同步等）没有删除，新场景全部放在 `com.allst.es.scenario`，索引名以 `scenario_` 开头，避免和旧索引混在一起。

运行环境与当前工程一致：Java 8、Spring Boot 2.5.4、Spring Data Elasticsearch 4.2、Elasticsearch 7.12 客户端。集群地址、账号沿用 `application.properties`，默认 `https://127.0.0.1:9200`，用户 `elastic`。

## 场景一览

| 场景 | 索引 | 接口前缀 | 文档 |
| --- | --- | --- | --- |
| 全文搜索 | `scenario_article` | `/scenario/fulltext` | [01-全文搜索](01-全文搜索.md) |
| 实时分析 | `scenario_order_event` | `/scenario/analytics` | [02-实时分析](02-实时分析.md) |
| 地理数据应用 | `scenario_place` | `/scenario/geo` | [03-地理数据应用](03-地理数据应用.md) |
| 日志和事件数据分析 | `scenario_app_log` | `/scenario/log` | [04-日志与事件数据分析](04-日志与事件数据分析.md) |
| 安全信息和事件管理 | `scenario_security_event` | `/scenario/siem` | [05-安全信息与事件管理](05-安全信息与事件管理.md) |
| 搜索建议 | `scenario_suggest` | `/scenario/suggest` | [06-搜索建议](06-搜索建议.md) |
| 电商分面检索 | `scenario_product` | `/scenario/product` | [07-电商分面检索](07-电商分面检索.md) |

`GET http://127.0.0.1:4565/scenario` 会返回同一份目录。

## 共同约定

1. 检索字段用 `text`，过滤、排序、聚合字段用 `keyword`、数值、日期或 `geo_point`。中文正文使用 IK：索引 `ik_max_word`，搜索 `ik_smart`。本仓库已有 IK 安装记录，见 `README-ES.md`。没有安装 IK 时，创建索引会失败，需要把对应 `@Field` 的 analyzer 改成 `standard`。
2. 实体上 `createIndex = false`。应用启动不连 Elasticsearch。调用 `/index/init`、`/seed` 或第一次查询时，才按实体注解创建 mapping。
3. 样例写入后会 `refresh`，方便立刻查到。生产环境不要对每条写入都 refresh，使用索引的 `refresh_interval`。
4. 时间参数接受 `yyyy-MM-dd`、`yyyy-MM-dd HH:mm:ss`、`yyyy-MM-ddTHH:mm:ss`。写入映射格式是 `date_hour_minute_second`。
5. 分页从 0 开始，单页最大 50。成功响应 `code` 为 0；参数错误返回 HTTP 400，集群错误返回 HTTP 500。异常处理只覆盖 `com.allst.es.scenario`，不影响旧接口。
6. `/seed` 使用固定文档 id，重复调用会覆盖，不会无限追加。

PowerShell 里请用 `curl.exe`，`curl` 是 `Invoke-WebRequest` 的别名。

```bat
curl.exe http://127.0.0.1:4565/scenario
```

## 代码怎么分层

每个场景都是同一条链路：`Document` 描述 mapping，`Service` 组织查询，`Controller` 只做参数接收。公共能力在 `com.allst.es.scenario.common`：

- `IndexSupport`：按实体创建索引并刷新
- `ScenarioTimes`、`EsQuerySupport`：时间窗口和 range 过滤
- `AggregationReads`：把 terms、date_histogram、filter 聚合读成 `NamedCount` / `TrendPoint`
- `ScenarioExceptionHandler`：场景接口的错误返回

查询主路径使用 `ElasticsearchRestTemplate` + `NativeSearchQuery`。搜索建议是例外：Spring Data Elasticsearch 4.2 的 `SearchHits` 没有 suggest 结果，所以补全查询使用项目里已经配置好的 `RestHighLevelClient`。

## 参考来源

实现时对齐的是本仓库已经在用的 Spring Data Elasticsearch 4.2 API，而不是 Spring Boot 3 的新 Java Client。查询形态参考 Elasticsearch 官方文档里的几类能力：

- [Full text queries](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/full-text-queries.html)
- [Aggregations](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/search-aggregations.html)
- [Geo queries](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/geo-queries.html)
- [Completion suggester](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/search-suggesters-completion.html)

分面检索采用常见的 `query` + `post_filter` + `filter` 聚合：文本查询决定相关性和聚合范围，已选筛选条件只作用于命中列表，每个分面聚合再排除自己的筛选，避免选中一个品牌后品牌列表只剩一项。SIEM 只做检测统计，规则判断抽成 `SiemRuleEngine`，不依赖集群就能回归。
