# 09 轻量事务、TTL、计数器与集合

## 场景

遥测用普通 `INSERT`。另外三种写入和它不是一条路径，不能放进同一个 `BATCH`：

| 路径 | 表 | 接口 | 幂等 |
| --- | --- | --- | --- |
| 轻量事务 | `sku_stock` | `/stock`、`/stock/cas` | 否。条件可能已经变了 |
| 计数器 | `device_hit_counter` | `/counters` | 否。重试会再加一次 |
| 集合 | `device_profile` | `/profiles` | 整行替换和单个阈值是幂等的。set 追加按非幂等处理 |

库存接口固定使用 `LOCAL_QUORUM` 和 `LOCAL_SERIAL`，不跟随遥测的 `LOCAL_ONE`。`LOCAL_SERIAL` 是 Paxos 那一轮的一致性，`LOCAL_QUORUM` 是条件通过之后真正写入的一致性。单机房用 `LOCAL_SERIAL`，不要用跨机房的 `SERIAL`。

## 轻量事务

创建：

```cql
INSERT INTO sku_stock (sku_id, warehouse, quantity) VALUES (?, ?, ?) IF NOT EXISTS
```

扣减或改数量：

```cql
UPDATE sku_stock SET quantity = ? WHERE sku_id = ? IF quantity = ?
```

`applied` 为 false 不是异常。创建失败说明这行已经有了；比较并设置失败时，结果里带当前的 `quantity`，调用方用新的 `expected` 再试。轻量事务比普通写入慢一个数量级以上，只给库存、名额这种冲突少、必须互斥的键。不要给每条遥测加 `IF NOT EXISTS`。

## TTL

表级 TTL 是 7 天，写在 `CREATE TABLE` 上。单行要更短，就在写入遥测时传 `ttlSeconds`。

不接受 0。在 CQL 里 `USING TTL 0` 表示这一行不设 TTL，会把表级 7 天取消掉。

static 列不和带 TTL 的遥测写在一条语句里。见 [01-基本写入](01-基本写入.md)。

## 计数器

```cql
UPDATE device_hit_counter SET hits = hits + ? WHERE device_id = ? AND day_bucket = ? AND metric_type = ?
```

计数器表里除了主键只能有 counter 列，不能设 TTL，不能 `INSERT`，也不能和普通列打在一个批次里。这个接口的 `delta` 是 1 到 1000 的正数。减量可以让计数器变成负数，而且失败重试分不清加没加过，所以不提供。

读计数器可能短时间偏旧，或者在修复期间偏大。不要拿它做余额。

## 集合

`device_profile` 使用非冻结的 `set`、`map`、`list`，才能追加和改单个键。冻结集合只能整格替换。

| 操作 | CQL | 代价 |
| --- | --- | --- |
| 整行替换 | `INSERT` 已提交的列，空列 `unset` | 一次写入 |
| 追加标签 | `labels = labels + ?` | 先读这一格再写回 |
| 改一个阈值 | `thresholds[?] = ?` | 先读 map 再写回 |

集合更新因此不要放在遥测热分区上。档案一行一个设备。

## 代码

- `com.allst.boot.mutation.MutationService`
- `com.allst.boot.mutation.MutationRules`

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/cassandra/mutations/stock` | `IF NOT EXISTS` 创建库存 |
| POST | `/api/cassandra/mutations/stock/cas` | `IF quantity = expected` |
| GET | `/api/cassandra/mutations/stock` | 读库存 |
| POST | `/api/cassandra/mutations/counters` | 计数器加量 |
| GET | `/api/cassandra/mutations/counters` | 读一台设备一天的计数 |
| PUT | `/api/cassandra/mutations/profiles` | 写档案 |
| POST | `/api/cassandra/mutations/profiles/labels` | 追加 set |
| POST | `/api/cassandra/mutations/profiles/thresholds` | 更新 map 的一个键 |
| GET | `/api/cassandra/mutations/profiles` | 读档案 |

## 调用示例

```bat
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/mutations/stock -H "Content-Type: application/json" -d "{\"skuId\":\"sku-001\",\"warehouse\":\"wh-a\",\"quantity\":10}"
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/mutations/stock/cas -H "Content-Type: application/json" -d "{\"skuId\":\"sku-001\",\"expected\":10,\"quantity\":9}"
curl.exe "http://127.0.0.1:8100/api/cassandra/mutations/stock?skuId=sku-001"
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/mutations/counters -H "Content-Type: application/json" -d "{\"deviceId\":\"dev-001\",\"day\":\"2026-10-24\",\"metricType\":\"temperature\",\"delta\":1}"
curl.exe -X PUT http://127.0.0.1:8100/api/cassandra/mutations/profiles -H "Content-Type: application/json" -d "{\"deviceId\":\"dev-001\",\"firmware\":\"1.2.0\",\"labels\":[\"edge\"],\"thresholds\":{\"temperature\":80},\"channels\":[\"a\",\"b\"]}"
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/mutations/profiles/labels -H "Content-Type: application/json" -d "{\"deviceId\":\"dev-001\",\"labels\":[\"vip\"]}"
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/mutations/profiles/thresholds -H "Content-Type: application/json" -d "{\"deviceId\":\"dev-001\",\"metricType\":\"humidity\",\"threshold\":70}"
```

遥测的单行 TTL 在写入接口上：

```bat
curl.exe -X POST http://127.0.0.1:8100/api/cassandra/readings -H "Content-Type: application/json" -d "{\"deviceId\":\"dev-001\",\"region\":\"cn-east\",\"metricType\":\"temperature\",\"value\":26.5,\"ttlSeconds\":3600}"
```

## 生产环境要注意

- 轻量事务的热点是一个分区键。所有请求打到同一个 `sku_id` 上，Paxos 会串行。
- 计数器超时后要靠业务去重，不能靠驱动重试。语句被标成非幂等。
- `list` 按位置改会读整列。能用 set 或 map 就不要用 list 做更新。
- 空集合不要绑定成 null。null 是墓碑，`unset` 才是“这次不改这一列”。
