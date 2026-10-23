# 33. 分页、排序与投影

这三件事经常一起出现，但不是同一个机制。

分页和排序是查询参数：

- 仓库：`Page<Employee> findByStatus(EmployeeStatus, Pageable)`、`List<Employee> findByStatus(EmployeeStatus, Sort)`
- 控制器：`EmployeeController.page` 和 `sorted`
- 默认大小和最大大小见 `spring.data.web.pageable.*`

投影是返回形状：

- 构造器投影：`EmployeeSummary`
- 封闭接口投影：`NameOnly`
- `@Query` 里的 `select e.id as id, e.name as name`

分页对象里面可以装投影，例如 `Page<EmployeeSummary>`。`EmployeeService.page` 是先分页实体再 `map`。数据量再大时，把投影写进查询，避免先查出整行和关联。

`join fetch` 加 `Pageable` 会退化成内存分页。订单用“先分页 id，再按 id 抓取集合”。排序要作用在第一条 id 查询上，第二条 `IN` 查询不再保证顺序，调用方需要按 id 列表排回去。当前示例只演示抓取，没有再做一次稳定排序。
