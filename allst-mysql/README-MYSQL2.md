# MySQL数据库
```text
数据结构动态演示： https://www.cs.usfca.edu/~galles/visualization/Algorithms.html

1、 索引
    索引是帮助MySQL“高效”获取数据的“排好序”的“数据结构”
    索引数据结构
        ❀二叉树
        ❀红黑树
        ❀Hash表
        ❀B-Tree

```
## MyCat
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
```