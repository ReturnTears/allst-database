《Elasticsearch全面解析与实践》

1.1 Elasticsearch概述

◆ Elasticsearch（简称ES）是一个开源的、高扩展的、分布式的、提供多用户能力的全文搜索引擎，也是一个基于Lucene搜索的服务器，可以近乎实时地存储和搜索数据

◆ 在云计算环境中，Elasticsearch能够达到数据的实时搜索，而且性能非常稳定，安装与使用也非常便捷

◆ Elasticsearch在Java、.NET、PHP、Python、Apache Groovy、Ruby等程序设计语言中都可以使用

◆ Elasticsearch能很方便地用于对大量数据进行搜索和分析，充分利用Elasticsearch的水平伸缩性，能够使数据在生产环境中变得更富有价值


1.2 Elasticsearch与Solr比较

◆ Elasticsearch部署和安装简单，并自带分布式协调管理功能；而Solr需要依赖ZooKeeper进行分布式协调管理

◆ Elasticsearch基本是开箱即用，解压之后就可以使用；相对而言，Solr使用难度较大

◆ Solr支持多种数据格式的文件，比如JSON、XML、CSV等；而Elasticsearch仅仅支持JSON数据格式的文件

◆ Solr数据搜索的速度快，但是数据插入和数据删除的速度都比较慢，它主要用于电商平台和数据搜索多的应用；而Elasticsearch创建索引（数据插入）和数据搜索的速度都比较快

◆ Solr是传统的搜索应用方案；而Elasticsearch更适用于新兴的近实时搜索

◆ Solr提供的功能繁杂；而Elasticsearch注重核心功能，高级功能大多数由第三方插件提供，例如图形化界面需要Kibana来支撑


1.3 为什么要学习Elasticsearch

◆ 在当前的软件行业中，搜索功能是软件系统或平台的基本功能，Elasticsearch可为相应的软件打造良好的搜索体验

◆ Elasticsearch具备非常强大的数据分析能力。虽然Hadoop也可以进行大数据分析，但是没有Elasticsearch这样强大的分析能力

◆ Elasticsearch使用方便，既可以将其安装在PC上，又可以部署在生产环境中

◆ 在当今的大数据时代，具备了近实时的搜索和分析能力，企业才能拥有核心的竞争力，才能洞见未来


1.4 Elasticsearch的主要功能及应用场景

◆ 海量数据的分布式存储以及集群管理，能达到服务与数据的高可用以及系统架构的水平扩展

◆ 近实时的数据搜索能力，能够对结构化数据、全文数据、地理位置等类型的数据进行处理和分析

◆ 海量数据的实时分析功能和各种强大的聚合功能。
Elasticsearch的主要应用场景如下：
1）网站搜索、代码搜索等。
2）日志管理、日志分析、日志安全指标监控、应用性能监控、Web抓取舆情分析等

◆ 利用Elasticsearch的高性能和分布式部署特征，可以对海量的业务订单数据进行分析和处理，还能利用Elasticsearch的聚合函数和分析能力统计出各种各样的数据报表

◆ Elasticsearch是与Logstash（数据收集和日志解析引擎）和Kibana（数据分析和可视化平台）一起开发的，这3个产品被设计成一个集成的解决方案（ELK），被广泛运用于大数据近实时分析领域，包括日志分析、指标监控、信息安全等

◆ 图1-3　Kibana生成的数据面板

◆ Elasticsearch除了和Kibana结合之外，还可以单独使用来实现数据库存储并且对数据进行全文搜索

◆ Logstash是一个动态数据收集管道，它拥有可扩展的插件生态系统，支持从不同来源收集数据和转换数据（过滤和处理），并将转换后的数据发送到不同的存储库中

◆ Logstash具有如下特点

◆ ）实时性。可实时解析数据并对数据进行过滤处理

◆ 可扩展性。具有200多个插件，可接收的数据来源有文本数据以及Redis、Kafka、MQ等存储的数据

◆ ）可靠性与安全性。Logstash会通过持久化队列来保证至少将数据送达一次，同时对数据进行传输加密

◆ 实时监控能力。对可以接收的数据源进行监控，一旦数据源产生新的数据就立刻传输

◆ Beats+MQ+Logstash+Elasticsearch+Kibana结合方案的架构图


1.5 Elasticsearch的安装

◆ Elasticsearch是一个非常占用内存的程序，如果只是为了学习如何使用Elasticsearch，则可以在启动过程中设置使用内存的限制

◆ 配置文件为Elasticsearch根目录下的config文件夹中的jvm.options文件。可使用如下两个参数进行内存大小限制的配置：

◆ 修改配置文件之后，重新启动即可。如果只是学习如何使用Elasticsearch，建议把这两个参数值都修改成1GB或者512MB

◆ Docker Desktop是Docker在Windows 10和macOS操作系统上的官方安装文件，使用Docker Desktop安装依然要先在虚拟机中安装Linux，再安装Docker

◆ （1）安装Hyper-V虚拟机
Hyper-V是微软开发的虚拟机，类似于VMware或VirtualBox，仅适用于Windows 10。这是Docker Desktop for Windows所使用的虚拟机（这个虚拟机一旦启用，VirtualBox、VMware Workstation 15及以下版本将无法使用。如果读者必须在计算机上使用其他虚拟机，请不要使用Hyper-V）


2.1 Elasticsearch的基本概念和相关术语

◆ 集群（Cluster）：集群由一个唯一的名字标识，默认为Elasticsearch。集群名称非常重要，具有相同集群名称的节点才会组成一个集群。集群名称可以在配置文件中指定。后面章节介绍搭建集群的时候将会介绍配置方式。需要注意的是，Elasticsearch中一个节点也被称为集群

◆ 节点（Node）：用于存储集群的数据，参与集群的索引和搜索功能。像集群有名字一样，节点也有自己的名字，默认在启动时会以一个随机的UUID的前7个字符作为节点的名字，用户可以为其指定任意的名字。多个节点通过同一个集群名在网络中发现同伴组成一个集群。一个节点也可以是集群

◆ 索引（Index）：索引是一个文档数据的集合。每个索引都有唯一的名称，用户通过这个名称来操作它。一个集群中可以有任意多个索引。

◆ 类型（Type）：在一个索引中，可以存放不同类型的文档，如用户数据、订单数据等。一个索引中只存放一类数据

◆ 文档（Document）：用JSON格式来表示，存储在索引库中的一条数据

◆ 分片（Shard）：在创建索引时可以指定分成多少个分片来存储。每个分片本身也是一个功能完善且独立的“索引”，可以被放置在集群的任意节点上，从而实现负载均衡。合理的分片数量可以提高Elasticsearch服务的性能

◆ 复制（Replication）：一个分片可以有多个副本，以防止数据丢失和避免数据丢失后服务不可用

