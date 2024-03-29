# MySQL数据库
```text
数据结构动态演示： https://www.cs.usfca.edu/~galles/visualization/Algorithms.html

1、 索引
    索引是帮助MySQL“高效”获取数据的“排好序”的“数据结构”
    索引数据结构
        ❀二叉树
        ❀红黑树(二叉平衡树)
        ❀Hash表
        ❀B-Tree
	B-Tree
		叶子节点具有相同的深度，叶节点的指针为空
		所有索引元素不重复
		节点中的数据索引从左到右递增排列
	B+Tree(B-Tree的变种)
		非叶子节点不存储data,只存储索引(冗余),可以放更多的索引
		叶子节点包含所有索引字段
		叶子节点用指针连接，提高区间访问的性能
		MySQL底层就是使用的B+Tree
	【B-Tree与B+Tree的区别】
	1、B-Tree的关键字，指针和数据都是存储在一起的，而B+Tree的非子叶只存储指针和关键字。数据存储在子叶节点中
	2、在B-Tree中越靠近根节点的数据查询的速度越快，在B+Tree中每个数据记录的查找时间几乎相同。每次查询都需要从根节点走到叶节点。
        在实际使用中B+Tree的性能更好。因为B+Tree的非子叶节点不存储数据，每个节点能够存储更多的键值，能过减少磁盘的访问次数，一次磁盘的访问次数相当于很多次的内存比较次数。
        所以B+Tree使用性能更高。
	树的高度可控
	
	MyISAM存储引擎索引实现
		MyISAM索引文件和数据文件是分离的(非聚集)
		MyISAM存储引擎是修饰数据库表的，不是修饰数据库的
		MyISAM存储引擎修饰的表数据是存储在本地磁盘中MYD文件中的，索引结构是存储在MYI文件中
	InnoDB索引实现(聚集)
		表数据文件(ibd格式文件)本身就是按B+Tree组织的一个索引结构文件
		聚集索引-叶节点包含了完整的数据记录
		为什么建议InnoDB表必须建主键，并且推荐使用整型的自增主键？
		【答】
			1、InnoDB采用B+树作为存储结构，那么必然需要一个列作为key
			2、一个B+树的节点可以存储key、地址、行数据(仅叶子节点)，key 就是不重复的值且可以比较
			3、主键的特定就是值不可重复，也不可为空，正好符合B+树key的要求
		【扩展】
			聚簇索引默认是主键，如果表中没有定义主键，InnoDB 会选择一个唯一的非空索引（确切说会选择一个唯一非空的列作为主键）代替。
			如果没有这样的索引，InnoDB 会隐式定义一个主键来作为聚簇索引。
			InnoDB 只聚集在同一个页面中的记录。包含相邻健值的页面可能相距甚远。
			如果你已经设置了主键为聚簇索引，必须先删除主键，然后添加我们想要的聚簇索引，最后恢复设置主键即可。
			聚簇索引性能最好而且具有唯一性，所以非常珍贵，必须慎重设置。一般要根据这个表最常用的SQL查询方式来进行选择，某个字段作为聚簇索引，或组合聚簇索引，这个要看实际情况
			
		为什么非主键索引结构叶节点存储的是主键值？(一致性和节省存储空间)
		【答】
			聚簇索引的数据的物理存放顺序与索引顺序是一致的，即：只要索引是相邻的，那么对应的数据一定也是相邻地存放在磁盘上的。
		联合索引的底层存储结构是什么样子？
		
	Hash结构
		对索引的Key进行一次hash计算就可以定位出数据存储的位置
		很多时候Hash索引要比B+树索引更高效
		仅能满足“=”，“IN”，不支持范围查询
		hash冲突问题
	
```
## MySQL高级-MyCat
```text
MyCat是一个实现了MySQL协议的服务器，是Java语言编写的数据库中间件
MyCat是居于阿里开源的Cobar产品研发的，Cobar的稳定性，可靠性，优秀的架构和性能以及众多成熟的使用案例使得MyCat变得非常强大
MyCat发展到目前版本，已经不是一个单纯的MySQL代理了，它的后端可以支持MySQL，SQL Server，Oracle， DB2，PostgreSQL
MyCat官网：http://www.mycat.org.cn/

使用MyCat后的结构：
Java应用 => MyCat中间件 => (数据库1 | 数据库2)

使用MyCat的优势：
1、数据量级
    单一的MySQL其数据存储量级和操作量级有限
    MyCat可以管理若干MySQL数据库，同时实现数据的存储和操作
2、开源性质
    MyCat是Java编写、开源、免费的中间件
    有非常多人和组织对其进行开发、维护、管理、更新
    MyCat版本提升较快，可以跟随环境发展，如果有问题，可以快速解决
    MyCat有开源网站和社区，且有官方发布电子书籍
    MyCat是阿里cobar转型而来
3、市场应用
    MyCat在互联网应用占比非常高

MyCat中的概念：
1、切分
    逻辑上的切分，在物理层面，是使用多库[database]，多表[table]实现的切分
    纵向切分(垂直切分)
    就是把原来存储于一个库的数据存储到多个库上，对同一个库进行读写操作并不能解决大规模并发写入问题
    有点：
        减少增量数据写入时的锁对查询的影响
        由于单表数量下降，常见的查询操作由于减少了需要扫描的记录，使得单表单次查询所需检索行数变少，减少了磁盘IO，延时变短
    缺点：
        无法解决单表数据量太大的问题
    
    横向切分(水平切分)
    把原来存储于一张表的数据分块存储到多个表上，当一个表中的数据量过大时，我们可以把该表的数据按照某种规则进行划分
    然后存储到多个结构相同的表，和不同的库上。
    有点：
        单表的并发能力提高了，磁盘I/O性能也提高了
        如果出现高并发的话，总表可以根据不同的查询，将并发压力分到不同的小表里面
    缺点：
        无法实现表连接查询
2、逻辑库-Schema
    MyCat中定义的database是逻辑上存在的，但是物理上是不存在的
    主要是针对纵向切分提供的概念
3、逻辑表-table
    MyCat中定义的table是逻辑上存在的，但是物理上是不存在的
    主要是针对横向切分提供的概念
4、默认端口
    MySQL默认端口是3306
    MyCat默认端口是8066
    Tomcat默认端口是8080
    Oracle默认端口是1521
    Nginx默认端口是80
    http默认端口是80
    redis默认端口是6379
5、数据主机-dataHost
    物理MySQL存放的主机地址，可以使用主机名,IP,域名定义
6、数据节点-dataNode
    配置物理的database，数据保存的物理节点就是database
7、分片规则
    当控制数据的时候，如何访问物理的database和table就是访问dataHost和dataNode的算法，
    在MyCat处理具体的数据CRUD的时候，如何访问dataHost和dataNode的算法：如，hash算法、crc32算法

MyCat的使用
1、读写分离
    原理：需要搭建主从模式，让主数据库(master)处理是事务性增、删、改操作，而从数据库(slave)处理查询操作
    MyCat配合数据库本身的复制功能，可以解决读写分离的问题
2、主从备份概念
    什么是主从备份，就是一种主备模式的数据库应用
    主库和备库数据完全一致
    实现数据的多重备份，保证数据的安全
    可以在master[innodb]和slave[myisam]中使用不同的数据库引擎，实现读写分离
    MySQL5.5+默认支持主从备份

在mycat的安装目录bin目录下执行如下命令：
1、安装mycat服务
.\mycat.bat install
2、启动mycat服务
.\mycat.bat start
.\mycat.bat restart
3、查看mycat状态
.\mycat.bat status
4、关闭mycat服务
.\mycat.bat stop
5、启动mysql
mysql -uroot -h127.0.0.1 -P8066 -p （密码为自己配置的密码， 我这里是root）
```



## MySQL高级

___MySQL架构原理和存储机制___

[MySQL体系架构图](src/main/resources/pic/mysql-server1.jpg)

```text
MySQL Server架构自顶向下大致可以分网络连接层、服务层、存储引擎层和系统文件层。
```

## MySQL高级-集群架构
1、集群架构设计主要遵循的三个维度：

   + 可用性
   - 扩展性
   * 一致性 
> 可用性设计
   + 站点高可用，冗余站点
   - 服务高可用，冗余服务
   * 数据高可用，冗余数据
```text
保证高可用的方法就是冗余，但是数据冗余带来的问题就是数据一致性问题
实现高可用的方案有以下几种架构模式：
1、主从模式：（特点）简单灵活，能满足多种需求，比较主流的用法，但是写操作高可用需要自行处理。
2、双主模式：（特点）互为主从，有双主双写，双主单写两种方式，建议使用双主单写
```

> 扩展性设计
```text
扩展性主要围绕着读操作和写操作扩展展开
```
+ 如何扩展以提高读性能
   + 加从库：简单易操作，方案成熟；从库过多会引发主库性能损耗，建议不要作为长期的扩充方案， 应该设法用良好的设计避免加从库来缓解读性能问题。
   + 分库分表
+ 如何扩展以提高写性能
   + 分库分表

> 一致性设计



## MySQL高级-分库分表

垂直拆分

+ 垂直分库

+ 垂直分表

水平拆分

+ 水平分库
+ 水平分表



## MySQL高级-中间件

ShardingSphere

+ Sharding-JDBC

  - 数据分片
    - 分库分表
    - 读写分离
    - 分片策略
    - 分布式主键
  - 分布式事务
    * 标准化的事务接口
    * XA强一致性事务
    * 柔性事务
  - 数据库治理
    - 配置动态化
    - 编排和治理
    - 数据脱敏
    - 可视化链路追踪

  **Sharding-JDBC初始化流程：**

  。。。

  **数据分片核心概念：**

  + 分片算法
    - 精确分片算法
    - 范围分片算法
    - 复合分片算法
    - Hint分片算法
  + 分片策略
    + 标准分片策略
    + 复合分片策略
    + 行表达式分片策略

+ Sharding-Proxy

  ​	以Windows系统下sharding-proxy为例：

  + 下载apache-shardingsphere-sharding-proxy
  + 解压到指定的目录
  + 添加MySQL连接驱动包到lib文件夹中
  + 修改conf下配置文件
  + 启动bin目录下start.bat脚本
  + 启动完成后使用MySQL-CLI命令行启动：mysql -h 127.0.0.1 -P 3307 -u root -p  （密码也为root，这里对应前面的server.yaml文件中的配置）
    
  
  sharding异常之no table route info_
+ Sharding-Sidecar



## MySQL高级-运维工具

+ **Yearning**

  Yearning是开源的MySQL SQL语句审核平台，提供数据库字典查询，查询审计，SQL审核，等功能

+ **Yearning 2.0**

  指南：https://guide.yearning.io/

+ **canal**

  canal主要用途是基于MySQL数据库增量日志解析，提供增量数据订阅和消费

  canal的工作原理类似MySQL主从同步原理

  指南：https://github.com/alibaba/canal/

+ **DataX**

  DataX是阿里巴巴内被广泛使用的离线数据同步工具、平台， 实现包括MySQL，Oracle，SQL Server，PostgreSQL，HDFS，Hive，ADS，HBase，TableStore(OTS)，MaxCompute(ODPS)，DRDS等各种异构数据源之间高效的数据同步功能

  指南：https://github.com/alibaba/DataX/

+ **percona-toolkit**

  percona-toolkit是一组高级命令行工具的集合，可以查看当前服务的摘要信息，磁盘检测，分析慢查询日志，查找重复索引，实现表同步等等。

  下载地址：https://www.percona.com/downloads/percona-toolkit/LATEST/

  在线指南：https://www.percona.com/doc/percona-toolkit/3.0/index.html

+ **MySQLMTOP**

  官方地址：https://www.lepus.cc/

+ **ELK**

  ELK 最早是 Elasticsearch（简称ES）、Logstash、Kibana 三款开源软件的简称 。

  

+ **Prometheus**

  Prometheus于2012年由SoundCloud创建，目前已经已发展为最热门的分布式监控系统。
  Prometheus完全开源的，被很多云厂商（架构）内置，在这些厂商（架构）中，可以简单部署
  Prometheus，用来监控整个云基础架构设施。比如DigitalOcean或Docker都使用普罗米修斯作为基础监控。
  Prometheus是一个时间序列数据库，它涵盖了可以绑定的整个生态系统工具集及其功能。
  Prometheus主要用于对基础设施的监控，包括服务器、数据库、VPS，几乎所有东西都可以通过
  Prometheus进行监控。
