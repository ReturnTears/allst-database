# 3. 用返回值解决实际问题

返回值决定了调用方要不要处理空、要不要一次装进内存、要不要把实体传到表现层。

| 返回值 | 适合的问题 | 位置 |
| --- | --- | --- |
| `Optional<Employee>` | 可能不存在，调用方必须处理空 | `findByEmail` |
| `boolean` / `long` | 只需要判断或计数 | `existsByEmail`、`countByDepartmentId` |
| `List` | 结果集可控 | 大多数条件查询 |
| `Page` | 要总数和总页数 | `findByStatus(status, pageable)` |
| `EmployeeSummary` | 列表页只要三列，避免把关联图传出去 | `summarizeByStatus` |
| `NameOnly` | 接口投影，按 getter 生成 select | `findNameOnlyByStatus` |
| `Stream` | 大批量遍历，用完必须关闭，并且要在事务里 | `streamByStatus` |
| `int` | 批量更新影响行数 | `updateStatus` |

控制器对外使用 `EmployeeSummary`，不直接把带懒加载的 `Employee` 交给 Jackson。需要部门名的那个接口是故意留下来演示 open-in-view 的，正常接口不要照抄。

`Page` 会多一条 count 查询。只要“下一页”而不要总数时，用 `Slice` 更合适。本项目的员工列表需要总数，所以用 `Page`。
