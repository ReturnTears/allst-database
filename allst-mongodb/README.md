# mongodb

## MongoDB在Linux的安装
```text
1.下载社区版 MongoDB 4.1.3
去官网下载对应的MongoDB 然后上传到Linux虚拟机
2.将压缩包解压即可
tar -zxvf MongoDB-linux-x86_64-4.1.3.tgz
3.启动
./bin/mongod
4.指定配置文件方式的启动
./bin/mongod -f mongo.conf
配置文件样例:
dbpath=/data/mongo/
port=27017
bind_ip=0.0.0.0
fork=true
logpath = /data/mongo/MongoDB.log
logappend = true
auth=false

参数 说明
dbpath 数据库目录，默认/data/db
port 监听的端口，默认27017
bind_ip 监听IP地址，默认全部可以访问
fork 是否已后台启动的方式登陆
logpath 日志路径
logappend 是否追加日志
auth 是开启用户密码登陆
config 指定配置文件

```

## mongo 在Windows安装
```text
1、登录MongoDB 官网下载页面https：//www.mongodb.com/try/download/community，在页面的右边可以找到MongoDB社区版的下载链接
2、解压刚刚下载的.zip包，E:\Program Files\mongodb-7.0.1
3、配置环境变量：MONGODB_HOME=E:\Program Files\mongodb-7.0.1 PATH=%MONGODB_HOME%\bin;
4、在MongoDB安装目录下提供具有如下内容的配置文件(mongod.conf)
# mongod.conf
# 配置与存储有关的信息
storage:
  dbPath: E:\Program Files\mongodb-7.0.1\data\db
  journal:
	enabled: true
# 指定与日志有关的信息
systemLog:
	destination: file
	quiet: true
	logAppend: false
	path: E:\Program Files\mongodb-7.0.1\logs\mongod.log
# 配置与网络有关的信息
net:
	port: 27017
	bindIp: 0.0.0.0

需要手动创建：data目录、logs目录
5、运行如下命令即可启动MongoDB服务器(注意：需要用管理员身份运行命令行cmd)。
mongod.exe --config "E:\Program Files\mongodb-7.0.1\mongod.conf" --install

6、启动服务器
net start MongoDB
也可以手动打开服务：services.msc进入服务手动开启
关闭服务：
net stop  MongoDB
```

## mongo shell 的启动
```text
启动mongo shell
./bin/mongo
指定主机和端口的方式启动
./bin/mongo --host=主机IP --port=端口
```

## MongoDB的基本操作
```text
查看数据库
show dbs;
切换数据库 如果没有对应的数据库则创建
use 数据库名;
创建集合
db.createCollection("集合名")
查看集合
show tables;
show collections;
删除集合
db.集合名.drop();
删除当前数据库
db.dropDatabase();
```

## MongoDB的应用场景
```text

```

## Mongo

## 报错解决
```text
运行时报错如下：
java.lang.NoSuchMethodError: com.mongodb.connection.ConnectionPoolSettings$Builder.maxWaitQueueSize(I)

解决思路：
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.5.5</version>
    <relativePath/>
</parent>
将springboot parent依赖修改为如下
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.5.RELEASE</version>
    <relativePath/>
</parent>
```