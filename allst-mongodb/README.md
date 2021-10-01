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
