# 9. JpaSpecificationExecutor 的实现方式

`EmployeeRepository` 继承 `JpaSpecificationExecutor` 之后，就有了 `findAll(Specification)`、`findAll(Specification, Pageable)` 和对应的 count。

`EmployeeSpecifications` 把每个条件做成一个方法。条件不成立时返回 `null`，`Specification.where(...).and(...)` 会跳过 null。调用方只组合自己拿到的条件：

```text
nameContains + statusIs + salaryAtLeast + departmentName
```

执行时 Spring Data 把 `Specification` 转成 CriteriaQuery：`toPredicate(root, query, criteriaBuilder)`。每次调用都是一条新 SQL，不会先查全表。

和自定义 Repository 里手写的 Criteria 相比，Specification 的优势是可以复用、可以分页，并且仍然走 Spring Data 的排序参数。`EmployeeRepositoryCustomImpl.search` 是同一类动态条件的另一种写法，适合条件已经固定成一个搜索对象、还要精细控制 join 的时候。

部门这种关联要自己 `root.join("department")`。忘记 join 会在运行时找不到属性，而不是在编译期失败。编译期安全的写法是 Querydsl。
