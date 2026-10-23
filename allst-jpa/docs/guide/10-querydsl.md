# 10. Querydsl

`EmployeeRepository` 继承 `QuerydslPredicateExecutor`。实体编译时由 `querydsl-apt` 的 jpa 分类器生成 `QEmployee`。

`EmployeeService.byQuerydsl` 的写法：

```text
QEmployee.employee.name.containsIgnoreCase(name)
QEmployee.employee.status.eq(status)
BooleanBuilder 把可选条件 and 起来
repository.findAll(predicate, Sort.by("id"))
```

路径写错会在编译失败，这是它和字符串版 Criteria、Specification 的差别。

生成配置在 `pom.xml` 的 `maven-compiler-plugin`：

- 处理器同时保留 Lombok 和 MapStruct，避免旧的 `PersonMapper` 停止生成。
- `-Aquerydsl.generatedAnnotationClass=javax.annotation.processing.Generated`，JDK 11 里没有 `javax.annotation.Generated`。
- 生成类在 `target/generated-sources/annotations`。

`findAll(Predicate)` 返回 `Iterable`。需要分页时用 `findAll(Predicate, Pageable)`。
