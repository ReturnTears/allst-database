# 2. 查询方法的命名语法与参数

Spring Data 解析的是方法名，不是注释。前缀可以是 `find`、`read`、`query`、`get`、`count`、`exists`、`delete`、`stream`。前缀后面用 `By` 开始条件。

`EmployeeRepository` 里能直接对上的例子：

| 方法 | 含义 |
| --- | --- |
| `findByEmail` | 等值 |
| `findByNameContainingIgnoreCase` | 忽略大小写的包含 |
| `findByAgeBetweenAndStatus` | Between 和 And |
| `findByDepartmentName` | 路径表达式，实际是 `department.name` |
| `findTop5ByStatusOrderBySalaryDesc` | 限制条数并排序 |
| `existsByEmail` / `countByDepartmentId` | 不把整行查回来 |
| `findByIdIn` | 集合参数 |

参数规则：

- 条件参数按方法名里的属性顺序排列。
- `Pageable` 和 `Sort` 是特殊参数，不占用 `By` 后面的属性。
- 同一个方法名可以有 `List`、`Page`、`Sort` 几种重载，靠参数类型区分。
- `deleteBy...` 和 `@Modifying` 必须跑在事务里。

分页、排序的 Web 入口见 [15](15-webmvc.md) 和 [33](33-page-sort-projection.md)。
