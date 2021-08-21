# Lucene
Lucene是一个开源的，高性能的，信息搜索库（IR：Information Retrieval Library）。

数据分类：
+ 结构化数据：
+ 非结构话数据（又叫全文数据）：

全文数据查询方法：
+ 1、顺序扫描法
```text
所谓顺序扫描，就是要找内容包含一个字符串的文件，就是一个文档一个文档的看，对于每一个文档，
从头看到尾，如果此文档包含此字符串，则此文档为我们要找的文件，接着看下一个文件，直到扫描完
所有的文件。
这种方法是数据量大就搜索慢。
```
+ 2、全文检索
```text
全文检索是指计算机索引程序通过扫描文章中的每一个词，对每一个词建立一个索引，指明该词在文章
中出现的次数和位置，当用户查询时，检索程序就根据事先建立的索引进行查找，并将查找的结果反馈
给用户的检索方式。这个过程类似于通过字典中的检索字表查字的过程。
```

Lucene特性：``

使用luke工具查看Lucene索引
+ [查看索引1](src/main/resources/picture/luke_1.png)
+ [查看索引2](src/main/resources/picture/luke_2.png)
+ [查看索引3](src/main/resources/picture/luke_3.png)

Lucene底层存储结构


Lucene相关度排序


Lucene使用注意事项


## 注意实现
```text
使用索引搜索工具luke搜索我们创建的索引时， 需要版本匹配一致。
org.apache.lucene:8.9.0版本配合luke-swing-8.0.0-luke-release使用时会报错：
cannot open index path . not a valid lucene index directory or corrupted see
解决方法:
将lucene版本修改为：org.apache.lucene:7.7.3

```

# Solr
Solr是一个建立在Lucene基础上的 企业级的 快速的 可扩展的 可部署的 搜索和存储引擎。

Solr官网：
https://solr.apache.org/ 
Solr下载地址
wget https://downloads.apache.org/lucene/solr/7.7.3/solr-7.7.3.tgz

解压：
tar -xvf solr-7.7.3.tgz
启动：
cd solr-7.7.3/
bin/solr start -force -port 端口号
-force 为以root用户启动时必须加的命令 若不是root用户启动则无需加该命令
端口号也可以不指定， 默认端口为：8983

使用浏览器访问主机：http://host:port
http://192.168.0.100:8983/
如果访问不了，查看防火墙是否关闭。
查看防火墙状态： systemctl status firewalld
关闭防火墙： systemctl stop firewalld
[访问成功页面](src/main/resources/picture/solr.jpg)



