# 8. QueryByExampleExecutor

`JpaRepository` 已经继承了 `QueryByExampleExecutor`，所以 `EmployeeRepository` 没有再写这个接口。

用法在 `EmployeeService.byExample`：

1. 用一个 `Employee` 当探针，只填本次要匹配的字段。
2. `ExampleMatcher.matching()` 忽略 null。
3. 姓名使用 `contains().ignoreCase()`。
4. `version`、`projects`、`profile` 放进 `withIgnorePaths`。空集合不是 null，不忽略的话会参与匹配，结果经常是空。

Example 适合“表单上有什么就拼什么”的等值和包含。它不适合 OR、子查询、范围组合得很深的条件。那种查询用 [Specification](09-specification.md) 或 [Querydsl](10-querydsl.md)。

匹配发生在数据库，不是把全表查到内存再过滤。
