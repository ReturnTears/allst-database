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