# ClickHouse概述 
```text
ClickHouse是一个用于OLAP（On-Line Analytical Processing，在线分析处理）的列式数据库管理系统(Columnar DBMS)，
由俄罗斯Yandex公司的程序员在2008年开始开发（使用C++编程语言），并于2016年6月15日开源。
为什么叫ClickHouse呢？其实原因很简单。这款产品的设计初衷是解决用户点击流的大数据数仓分析问题。ClickHouse即Click Stream和Data WareHouse的统称。

面对海量数据（TB级）、复杂业务分析场景问题，ClickHouse能够实现基于SQL语法的实时查询秒级响应。
ClickHouse使用SIMD高效指令集、向量化执行引擎，在查询性能方面较传统方式提升了100～1000倍，同时具备50MB/s～200MB/s的实时导入能力，支持列存储数据高压缩率。

OLAP是数仓的灵魂，主要用于对复杂多维、大规模数据集进行在线实时分析，为用户提供决策支持。OLAP的核心是A(Analytical)，也就是在线实时分析。

OLTP的重点是T(Transaction)，也就是在线实时事务处理。OLTP是传统关系型数据库服务(Relational Database Service，RDS)的主要应用场景，主要用来存储与业务高度相关的数据。


```
## ClickHouse 特性
```text
ClickHouse基于OLAP场景需求，定制开发了一套全新、高效的列式存储引擎，实现了数据有序存储、主键排序、块级索引（主键索引、稀疏索引）、数据分区、数据分片、本机存储、多重缓存、TTL、主从复制等丰富的功能特性，这些功能特性共同为ClickHouse极致的分析性能奠定了基础。

ClickHouse实现了单机多核并行、多线程、分布式计算、向量化执行和SIMD指令、LLVM运行时代码生成(Runtime Code Generate)等多项重要技术，进一步为ClickHouse的“快”提供了技术支持。ClickHouse的性能大幅超越了很多商业MPP数据库软件，比如Vertica、InfiniDB等。

ClickHouse的关键特性有深度列存储、向量化查询执行引擎(Vectorized Query Execution)、数据压缩(Data Compression)、使用磁盘、支持SQL、实时数据更新、稀疏索引、运行时代码生成、支持近似计算、数据TTL、高吞吐写入能力、多核心并行计算、多服务器分布式计算、
分布式MPP计算架构、分片和副本、完整的DBMS能力、自适应连接算法(Adaptive Join Algorithm)、数据复制和数据完整性、提供复杂数据类型和丰富的函数库等。


1、 深度列存储

```

