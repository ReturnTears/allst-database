# allst-trino

Spring Boot 2.7 集成 Trino JDBC 的 demo。应用通过独立的 `JdbcTemplate` 查询 Trino，默认只读，并提供目录、schema、表和自定义 SQL 接口。

集成步骤、配置说明和回溯记录见 [docs/TRINO_INTEGRATION.md](docs/TRINO_INTEGRATION.md)。

- 应用端口：`8122`
- Trino 协调节点端口：`8080`
- 驱动与服务端：`435`
- 示例数据：官方镜像自带的 `tpch` catalog

```bash
docker compose up -d
mvn spring-boot:run
```

```bash
curl http://127.0.0.1:8122/trino/halo
curl http://127.0.0.1:8122/trino/health
```

下面是原先整理的 Trino 安装笔记。

# Trino
```
官方地址：https://trino.io/

docker 安装Trino
0、使用docker命令下载容器镜像，将其命名为trino-trial并保存在本地，然后在后台运行它，并将容器内的8080端口映射到工作站上的8080端口
   docker run -d -p 8080:8080 --name trino-trial trinodb/trino
1、连接到容器
   docker exec -it trino-trial trino
2、使用trino命令行界面CLI执行trino命令，这会连接到同一个容器里运行着的Trino服务器。在命令行执行一个tpch 基准测试数据表上的查询：
   select count(*) from tpch.sf1.nation
3、如果想结束探索trino,使用quit命令退出
   quit
4、执行如下命令可以停止并移除之前的容器：
   docker stop trino-trial
5、使用如下命令可以重新开始：
   docker start trino-trial
   
使用jar包运行trino cli
java -jar .\trino-cli-435-executable.jar --version

java -jar .\trino-cli-435-executable.jar

使用本地jar包作为客户端链接docker上trino服务：
java -jar .\trino-cli-435-executable.jar --server 172.20.10.5:8080
trino>
接下来就可以和trino服务端进行交互了

Trino服务端提供了一个Web界面，通常叫作Trino Web UI。
默认用户名：admin
```