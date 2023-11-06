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
   
```