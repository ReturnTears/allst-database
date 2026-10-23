# 26. SpEL

项目里有三种用法，风险不一样。

## 仓库查询

`EmployeeRepository`：

- `:#{#keyword}` 使用方法参数。
- `:#{@salaryPolicy.minSalary}` 调用名为 `salaryPolicy` 的 Bean。

参数仍然是绑定变量，不是字符串拼接。Bean 方法应返回值，不要在表达式里写会改数据的逻辑。

## 配置注入

`SpelGuide` 上的 `@Value("#{salaryPolicy.minSalary}")` 在创建 Bean 时求值。不要在 Bean 自己的字段上引用自己，那样会在创建过程中循环依赖。

## 业务代码

`SpelGuide.salaryAboveFloor` 使用 `SpelExpressionParser` 和 `StandardEvaluationContext`，变量是 `#employee`、`#floor`。

不要把用户提交的字符串交给解析器。表达式应当来自代码或受控配置。需要 Spring Security 的 `?#{principal}` 时再引入安全模块，当前项目没有这个依赖。
