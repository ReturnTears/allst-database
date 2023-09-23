# ES
## ElasticSearch7.6.x+
```text
官方地址：
https://www.elastic.co/cn/elasticsearch/
Elasticsearch 是一个分布式、RESTful 风格的搜索和数据分析引擎，能够解决不断涌现出的各种用例。
ElasticSearch是基于Lucene做了一些封装和增强
ELK是Elasticsearch、Logstash、Kibana三大开源框架首字母大写简称。
ES是面向文档的,一切皆是JSON
关系型数据库与ES的客观对比:
MySQL               ES
数据库(database)     索引(indices)
表(tables)          types
行(rows)            documents
字段(columns)       fields
ES的核心概念::
集群:
节点:
索引:
类型:
文档:
分片:
映射:

ES(集群)中可以包含多个索引(数据库),每个索引中可以包含多个类型(表),每个类型下又包含多个文档(行)， 每个文档中又包含多个字段(列)
ES的物理设计:
ES在后台把每个索引划分成多个分片， 每个分片可以在集群中不同的服务间迁移

倒排索引:

```

## Windows下安装ElasticSearch
```text
在华为镜像服务器上下载相应的文件包:https://mirrors.huaweicloud.com/
elasticsearch、kibana、logstash
Windows下elasticsearch是开箱即用的，在config目录下，elasticsearch.yml中添加跨域配置:
http.cors.enabled: true
http.cors.allow-origin: 
启动elasticsearch:
点击bin目录下elasticsearch.bat脚本
启动完成后: http://127.0.0.1:9200

为ES的默认用户设置密码：
命令行输入：elasticsearch-setup-passwords interactive
密位设置： hadoop

为Elasticsearch集群创建一个证书颁发机构的证书:
命令行输入：elasticsearch-certutil ca
使用默认的证书文件名，密码设置为：567890

为Elasticsearch集群的每个节点生成各自的证书及私钥：
命令行输入：elasticsearch-certutil cert --ca elastic-stack-ca.p12
1、输入证书机构颁发的密码：567890
2、设置新的证书的名字：使用默认的证书名
3、输入新的证书密码：123456

使用elasticsearch.keystore管理证书文件的密码：
命令行输入：elasticsearch-keystore add xpack.security.transport.ssl.keystore.secure_password
会提示输入密码：123456
在配置文件elasticsearch.yml中配置了几处使用证书的地方，就需要执行几次上述命令
1、命令行输入：elasticsearch-keystore add xpack.security.transport.ssl.truststore.secure_password
提示输入密码为：123456
2、命令行输入：elasticsearch-keystore add xpack.security.http.ssl.keystore.secure_password
提示输入密码为：123456
3、命令行输入：elasticsearch-keystore add xpack.security.http.ssl.truststore.secure_password
提示输入密码为：123456

重启服务，输入如下命令来测试服务器：
使用curl来测试Elasticsearch服务器：curl -u elastic:hadoop -k https://localhost:9200
上面命令中的-u选项用于指定用户名和密码，-k选项用于指定忽略对方站点的SSL证书

下载可视化开源工具:https://github.com/mobz/elasticsearch-head
在elasticsearch-head-master目录中安装相关依赖包：
使用淘宝安装: cnpm install
启动elasticsearch-head: npm run start
启动完成后:http://127.0.0.1:9100
```

### Elasticsearch基本用法
```text
1、操作Index
1.2、添加Index使用PUT请求
命令行：curl -k -u elastic:hadoop -X PUT https://localhost:9200/fkjava
输出为：{"acknowledged":true,"shards_acknowledged":true,"index":"fkjava"}
1.3、查看当前有哪些Index
命令行：curl -k -u elastic:hadoop https://localhost:9200/_cat/indices
输出为：
green  open .security-7              zcophF-mSnytZCGJpVLx3w 1 0  6 0 19.6kb 19.6kb
yellow open fkjava                   7Xavx2X9TVyAq1nGDtQnrg 1 1  0 0   230b   230b
green  open .kibana_task_manager_1   FqkEIthfQmu1Kswq45mceQ 1 0  2 0  6.6kb  6.6kb
green  open .apm-agent-configuration I5_ietocQzexs4TDPCwcrA 1 0  0 0   283b   283b
yellow open my-es                    JrqUJL2mSPql8QxCRoH5_A 5 1  0 0  1.3kb  1.3kb
green  open .kibana_1                YsvraOl2QMOSx3iJyLW36w 1 0 14 4 23.6kb 23.6kb
1.3、删除Index使用DELETE请求
命令行：curl -k -u elastic:hadoop -X DELETE https://localhost:9200/fkjava
输出为：{"acknowledged":true}

2、操作文档
2.1、添加文档使用POST请求。
在命令行所在的当前路径下定义一个book.json文件
2.2、运行如下命令即可添加一个文档
命令行：curl -k -u elastic:hadoop -X POST https://localhost:9200/fkjava/book/1 -d @book.json -H "Content-Type:application/json"
输入：如图：操作文档.png

查看指定Index下的所有文档，运行如下命令即可:
命令行：curl -k -u elastic:hadoop https://localhost:9200/fkjava/_search?pretty=true
查看指定Index下指定ID的文档，运行如下命令即可：
命令行：curl -k -u elastic:hadoop https://localhost:9200/fkjava/book/1?pretty=true
删除指定ID对应的文档，运行如下命令即可:
命令行：curl -k -u elastic:hadoop -X DELETE https://localhost:9200/fkjava/book/1 
查询数据就是要求在文档的description字段中包含“全面”关键词：
命令行：curl -k -u elastic:hadoop https://localhost:9200/fkjava/_search -d @search.json -H "Content-Type: application/json"
输出为：查询文档关键字.png
如果要根据查询条件来删除文档，只要向Index后加“_delete_by_query”的URL地址发送POST请求即可：
curl -k -u elastic:hadoop -X POST https://localhost:9200/fkjava/_delete_by_query -d @search.json -H "Content-Type: application/json"
输出为：

```

## Windows下安装Kibana
```text
Windows下Kibana也是开箱即用的
在bin目录下: kibana.bat
Kibana启动后默认端口: 5601
在config目录下kibana.yml文件中配置:
i18n.locale: "zh-CN"实现汉化
```

## IK分词器
```text
https://github.com/medcl/elasticsearch-analysis-ik
使用Elasticsearch的插件机制即可安装IK分词器，运行如下命令来安装IK分词器:
命令行输入：
elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.6.1/elasticsearch-analysis-ik-7.6.1.zip
运行该命令将会自动从GitHub下载IK分词器压缩包，下载完成后请确认是否要安装IK分词器，选择y后继续安装。
安装过程查看：安装ik分词器.png

在项目目录下新建 fkjava.json文件
使用如下命令创建Index:
curl -k -u elastic:hadoop -X PUT https://localhost:9200/fkjava -d @fkjava.json -H "Content-Type:application/json"
该命令中增加了-H选项，设置Content-Type请求头的值为“application/json”；还增加了-d选项，用于读取fkjava.json文件的内容作为请求数据。
输出为：{"acknowledged":true,"shards_acknowledged":true,"index":"fkjava"}
如图：创建Index.png

在项目目录下新建 analyze.json文件
curl -k -u elastic:hadoop -X POST https://localhost:9200/fkjava/_analyze?pretty=true -d @analyze.json -H "Content-Type:application/json"
如图：测试ik分词器.png
每个词都被称作一个token，每个token都对应如下属性。
➢ start_offset：起始位置。
➢ end_offset：结束位置。
➢ type：类型。
➢ position：词的位置。

```