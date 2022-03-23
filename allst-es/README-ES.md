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

下载可视化开源工具:https://github.com/mobz/elasticsearch-head
在elasticsearch-head-master目录中安装相关依赖包：
使用淘宝安装: cnpm install
启动elasticsearch-head: npm run start
启动完成后:http://127.0.0.1:9100
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

```