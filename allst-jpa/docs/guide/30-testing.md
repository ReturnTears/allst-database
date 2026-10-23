# 30. 四类测试

指南测试使用 `GuideSliceApplication` 和 H2，不启动旧的 Mongo 仓库，也不连接 Redis / Elasticsearch。

| 类型 | 类 | 做法 |
| --- | --- | --- |
| Controller 单元测试 | `EmployeeControllerUnitTest` | standalone MockMvc，服务是 Mockito |
| Service 单元测试 | `EmployeeServiceUnitTest` | `@ExtendWith(MockitoExtension)`，仓库是假的 |
| Controller 集成测试 | `EmployeeControllerIntegrationTest` | `@SpringBootTest` + `@AutoConfigureMockMvc` + H2 |
| Service 集成测试 | `EmployeeServiceIntegrationTest` | 真实事务、真实仓库，覆盖 Example、Specification、Querydsl |

另外的集成行为放在：

- `GuideJpaBehaviorTest`：关联、继承、回调、N+1、懒加载、异步、计划缓存、事务传播
- `OptimisticLockRetryTest`
- `OpenInViewDisabledTest`
- `MultiDataSourceTest`、`ShardingSphereJpaTest`：不启动整个 Boot 容器，直接建 EntityManagerFactory

`src/test/resources/application.properties` 会盖住主配置里的同名文件，所以数据源、`ddl-auto` 和统计开关写在测试文件里。旧的 `UsersRepositoryTest` 仍连接 MySQL，没有改动。
