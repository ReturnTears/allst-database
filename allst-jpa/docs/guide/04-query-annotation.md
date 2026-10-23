# 4. `@Query` 语法与用法

方法名表达不了连接、投影和批量更新时，把 JPQL 写在 `@Query` 上。

`EmployeeRepository` 覆盖了这些形式：

- 对象查询：`select e from Employee e`
- `join fetch`：把部门一次查出，见 `findByDepartmentNameFetch`
- 构造器投影：`select new com.allst.boot.guide.dto.EmployeeSummary(...)`
- 接口投影：`select e.id as id, e.name as name`
- SpEL 参数：`:#{#keyword}` 和 `:#{@salaryPolicy.minSalary}`
- 批量更新：`@Modifying(clearAutomatically = true, flushAutomatically = true)`
- 原生 SQL：`nativeQuery = true`，分页时必须另给 `countQuery`

注意：

- 批量更新不走实体回调，也不会自动给 `@Version` 加一。改完后持久化上下文是脏的，所以打开了 `clearAutomatically`。
- `join fetch` 不要和 `Pageable` 写在同一条查询里，Hibernate 会在内存里分页并打出 HHH000104。订单分页改成先查 id，再按 id 抓集合，见 `CustomerOrderRepository`。
- 原生 SQL 的列名是物理列名。员工姓名列是 `emp_name`，不是 `name`。
- 枚举用了 `EnumType.STRING`，原生 SQL 里传 `ACTIVE` 这种名字。
