# 6. 关联关系

四个关系都落在员工模型上。

| 关系 | 谁持有外键 | 代码 |
| --- | --- | --- |
| 多对一 员工 → 部门 | `g_employee.department_id` | `Employee.department` |
| 一对多 部门 → 员工 | `mappedBy = "department"` | `Department.employees` |
| 一对一 档案 → 员工 | `g_employee_profile.employee_id` | `EmployeeProfile.employee` |
| 多对多 员工 ↔ 项目 | `g_employee_project` | `Employee.projects` |

JPA 规定多对一和一对一的默认抓取是 EAGER。这里全部写成 `LAZY`。否则查一个员工就会带出部门、档案和项目。

维护双向关系时走拥有方。`Department.addEmployee` 和 `Employee.addProject` 会同时改两边，避免内存里一边有、数据库另一边没有。

级联：

- 档案跟随员工 `CascadeType.ALL`，员工删除时档案一起删。
- 订单明细同样级联，并用 `orphanRemoval` 处理从集合里拿掉的行。
- 多对多不要级联删除。删员工不应该把项目删掉。

`@BatchSize` 放在项目集合和订单明细上，用来把后续的懒加载收成 `IN` 查询。它不能代替 fetch join，见 [25](25-n-plus-one.md)。
