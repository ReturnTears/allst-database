# Elasticsearch
```text
启动ES：bin/elasticsearch.bat
启动成功后访问地址：http://127.0.0.1:9200
启动Kibana: bin/kibana.bat
启动成功后访问地址：http://127.0.0.1:5601

查询所有的文档：
https://127.0.0.1:9200/_search

查询索引 books 下的文档：
https://127.0.0.1:9200/books/_search

查询所有的索引：
https://127.0.0.1:9200/_cat/indices?v

查询索引 video 下的文档：
https://127.0.0.1:9200/video/_search

原始DSL查询:
http://127.0.0.1:9200/video/_search
请求体：{
  "query": {
    "bool": {
      "must": [{
        "match": {
          "title": "架构"
        }
      }, {
        "match": {
          "description": "spring"
        }
      }, {
        "range": {
          "duration": {
            "gte": 10,
            "lte": 6000
          }
        }
      }]
    }
  }
}

聚合查询：
1、统计不同分类下的视频数量：
http://127.0.0.1:9200/video/_search
请求体：
{
  "size": 1,
  "aggs": {
    "category_group": {
      "terms": {
        "field": "category"
      }
    }
  }
}

分词查询
http://127.0.0.1:9200/video/_analyze
请求体：
{
  "field": "title",
  "text": "我今天去小滴课堂学习spring cloud项目大课"
}

```

## ES搜索引擎
```text
教程1
https://www.cnblogs.com/buchizicai/p/17093719.html

访问地址： https://www.cnblogs.com/xietingwei/p/18027313#springboot3x%E6%95%B4%E5%90%88es

```

# Kibana
```text
查询es_user索引：
GET /es_user/_search
{
  "query": {
    "match_all": {}
  }
}

```

# Error
```text
elasticsearch配置文件（elasticsearch.yml）中xpack相关属性设置为false
不然根据https请求的方式会报错, 待解决

访问地址： https://www.cnblogs.com/xietingwei/p/18027313#springboot3x%E6%95%B4%E5%90%88es


Java监听MySQL binlog日志文件报错：Client does not support authentication protocol requested by server; consider upgrading MySQL client
解决如下：
alter user 'root'@'localhost' identified with mysql_native_password by '12345678';
12345678 是密码

启动项目报错：
Field elasticsearchTemplate in com.allst.es.service.impl.UserServiceImpl required a bean of type 'org.springframework.data.elasticsearch.core.ElasticsearchTemplate' that could not be found.
解决如下：
从Spring Data Elasticsearch 4.0开始，ElasticsearchTemplate已经被弃用，并且推荐使用ElasticsearchRestTemplate或者直接使用RestHighLevelClient。

启动项目报错：
Factory method 'elasticsearchRestHighLevelClient' threw exception; nested exception is java.lang.IllegalStateException: could not create the default ssl context
解决如下：
ElasticConfig配置类中添加 RestHighLevelClient 配置对象

启动项目报错：
SpringBoot2整合ElasticSearch7.6.1报错：Host name 'localhost' does not match the certificate subject provided by the peer (CN=instance)
在配置的RestHighLevelClient对象中添加如下配置：
.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)

```

# Mysql
```text
监听MySQL BinLog日志
https://www.oryoy.com/news/java-jie-xi-mysql-binlog-shi-xian-shu-ju-shi-shi-tong-bu-yu-jian-kong-de-zui-jia-shi-jian.html

MySQL大表优化
https://www.cnblogs.com/HusterJin/p/13619548.html


ALTER TABLE table_name ENGINE=InnoDB;
ALTER TABLE table_name ENGINE=MyISAM;

测试监听MySQL Binlog写入到ES时， 往MYSQL的user表手动新增数据
use demo;
show variables like 'log_bin' ;
show tables;
select * from users u  ;
select * from user_info u  ;
CREATE TABLE `user`
(
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '姓名',
    age INT DEFAULT NULL COMMENT '年龄',
    email VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
    PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='user';
select * from user;

insert into user (id,name,age,email) values (24,'test_24',23,'test_24@163.com');
```

# SpringBoot
```text
Springboot启动后执行方法的四种方式:
1、注解@PostConstruct

2、CommandLineRunner接口

3、实现ApplicationRunner接口

4、实现ApplicationListener

四种方式的执行顺序
注解方式@PostConstruct 始终最先执行
如果监听的是ApplicationStartedEvent 事件，则一定会在CommandLineRunner和ApplicationRunner 之前执行。
如果监听的是ApplicationReadyEvent 事件，则一定会在CommandLineRunner和ApplicationRunner 之后执行。
CommandLineRunner和ApplicationRunner 默认是ApplicationRunner先执行，如果双方指定了@Order 则按照@Order的大小顺序执行，大的先执行。

```

# ElasticSearch
```text
springboot 整合 ES 有两种方案，ES 官方提供的 Elasticsearch Java API Client 和 spring 提供的 [Spring Data Elasticsearch](Spring Data Elasticsearch)

两种方案各有优劣

Spring：高度封装，用着舒服。缺点是更新不及时，有可能无法使用 ES 的新 API

ES 官方：更新及时，灵活，缺点是太灵活了，基本是一比一复制 REST APIs，项目中使用需要二次封装。

案例地址1：
https://www.cnblogs.com/konghuanxi/p/18094055

案例地址2：
https://www.cnblogs.com/xietingwei/p/18027313#springboot3x%E6%95%B4%E5%90%88es

案例地址3：
https://www.cnblogs.com/wan-ming-zhu/p/18080644
```

# Redis
```text
连接redis服务器
redis-cli -h localhost -p 6379
redis-cli -h r-xxx.redis.rds.aliyuncs.com -p 6379 -a xxxxxxx
发布队列消息
publish my_key '{"tag":"my_tag","accountMethod":"freightFund","idList":[50]}'
创建消费者组
xgroup create my-stream my-group-1 0 MKSTREAM
xread streams my-stream 0
xadd my-stream * user '{"tag":"my_tag","accountMethod":"freightFund","idList":[50]}'
xadd my-stream * user '{"tag":"my_tag","businessId":10086,"tableType":"10","body":"{\"name\": \"kang\",\r\n    \"age\": 18}"}'
查看未被消费的消息
xpending my-stream my-group-1
```

# 签名验证
```text
签名验证是一种常用的防止参数篡改的方法。其基本原理是：在发送请求时，客户端根据请求参数生成一个签名，并将签名作为请求的一部分发送给服务器。服务器在接收到请求后，根据相同的算法和参数重新生成签名，
并与客户端发送的签名进行对比。如果签名一致，则认为请求是合法的；否则，认为请求已被篡改。

为了实现签名验证，我们需要进行以下步骤：
定义签名算法：选择一个安全的哈希算法（如SHA-256）作为签名算法。
生成签名：客户端根据请求参数（不包括签名本身）和一个预定义的密钥，使用签名算法生成签名。
发送签名：客户端将生成的签名作为请求参数的一部分发送给服务器。
验证签名：服务器在接收到请求后，根据相同的算法、参数和密钥重新生成签名，并与客户端发送的签名进行对比。

```

# 接口参数加密解密方式其一
```text
SpringMVC 中给我们提供了 ResponseBodyAdvice 和 RequestBodyAdvice，利用这两个工具可以对请求和响应进行预处理。
<dependency>
    <groupId>com.allst.es</groupId>
    <artifactId>encrypt-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
项目源文件在allst-db/src/main/resources/file中，解压后可直接使用
```