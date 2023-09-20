# Solr

## 添加用户
```text
在users.json文件的所在文件夹执行如下命令：
curl --user root:32147 http://localhost:8983/solr/admin/authentication -H 'Content-type:application/json' -d @users.json
如图：setUser.png
```

## 删除用户
```text
在delete.json文件所在的文件夹下执行如下命令：
curl --user root:32147 http://localhost:8983/solr/admin/authentication -H 'Content-type:application/json' -d @delete.json
如图：deleteUser.png
```

## 管理Solr的Core
Solr 使用Core 来保存索引文档，Solr 的Core 有点类似于RDBMS的表。因此，在正式使用Solr之前，必须先创建Core
```text
Solr提供了两种方式来创建Core
1、Solr提供了两种方式来创建Core
solr create_core -c fkjava -d sample_techproducts_configs

2、通过图形用户界面创建Core


删除Core:
solr delete -c fkjava

```

## Git
```text
git提交报错：Error in the HTTP2 framing layer fatal
解决思路：git config --global http.version HTTP/1.1
git config --global http.sslVerify "false"
```