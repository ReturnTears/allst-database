# Presto
```text
第一部分 Presto 入门

◆ Presto 是一个 SQL 查询引擎，它使得用 SQL 访问任何数据源成为可能。你可以使用 Presto 通过水平扩展查询处理的方式来查询大型数据集。


第 1 章 Presto介绍

◆ Presto 包含两个分支：PrestoDB 和 PrestoSQL。2020 年 12 月，PrestoSQL 正式更名为 Trino。

◆ Presto 是一个开源的分布式 SQL 查询引擎，它是为了高效查询不同系统和各种规模（从 GB 级到 PB 级）的数据源而从头开始设计和编写的一套系统

◆ Presto 使用分布式执行来快速查询海量数据

◆ 分析师应该使用 Presto，因为他们期望 SQL 查询可以在几毫秒（实时数据分析）、几秒或几分钟内返回结果。Presto 支持 SQL，SQL 通常用在数据仓储、数据分析、海量数据聚合和报表生成等任务上，这些任务通常被归类为联机分析处理（online analytical processing，OLAP）

◆ Presto 同时使用了众所周知的技术和新颖的技术来执行分布式查询，这些技术包括内存并行处理、跨集群节点管线执行、多线程执行模型（以充分利用所有 CPU 核心）、高效的扁平内存数据结构（以最小化 Java 的垃圾回收）和 Java 字节码生成等

◆ 图 1-3：Presto 对多种数据源的 SQL 支持

◆ Presto 没有自己的存储，它只是在数据所在之处进行查询处理。使用 Presto 时，存储和计算是分离的，它们可以各自独立地扩展。Presto 代表计算层，底层的数据源代表存储层

◆ Presto 可以通过动态扩展计算集群的规模来扩展查询能力，并可以在数据源中数据所在的位置查询数据。借助这一特性，你可以极大地优化硬件资源需求并降低成本

◆ 图 1-4：用于所有数据源的多个使用场景的单一 SQL 访问点

◆ 图 1-5：Presto 官方网站的主页

◆ Presto 的详细文档作为代码库的一部分维护，可以从官方网站上获取到。该文档既包括概览，又包括 SQL 支持、函数、运算符、连接器、配置等方面的详细参考信息，还提供包含最新修改细节的发布记录


第 2 章 安装和配置 Presto

◆ 使用 docker 命令下载容器镜像，将其命名为 presto-trial 并保存在本地，然后在后台运行它

◆ Presto 可以运行在大多数现代 Linux 发行版和 macOS 上，它依赖 Java 虚拟机（JVM）和 Python 环境

◆ Presto 使用 Java 编写并要求你的系统内安装有 JVM。Presto 需要 Java 的长期支持版本 Java 11，不支持更旧版本的 Java，而在更新版本的 Java 环境下它也许可以正常工作

◆ Presto 的启动器脚本依赖 Python 2.6 或更高版本

◆ Presto 的二进制包使用 Maven 中心仓库分发。服务端应用程序可以通过一个 tar.gz 归档文件获得

◆ 安装目录包含下列子目录。
lib
包含组成 Presto 服务端及其全部依赖关系的 Java 归档文件（JAR）

◆ plugins
包含 Presto 插件及其依赖关系，各个插件分开存放在不同的目录下。Presto 默认已经包含许多插件，你也可以添加第三方插件。Presto 允许多种可插拔的组件，如连接器、函数和安全访问控制等与其集成

◆ bin
包含 Presto 的启动脚本。这些脚本用于启动、停止、重启、终结 Presto 进程，以及获得运行该进程的状态

◆ etc
配置文件所在的目录。此目录由用户创建并提供 Presto 必需的配置文件

◆ var
存放日志信息的数据目录。Presto 服务端第一次启动时会创建此目录，它默认情况下位于安装目录下

◆ 启动 Presto 前，需要先提供一些配置文件：
•  Presto 日志配置
•  Presto 节点配置
•  JVM 配置

◆ 想要在 Presto 里查询一些外部数据，这需要你将一个数据源配置为 catalog。

◆ Presto 的 catalog 定义了用户可用的数据源。数据访问是由 Presto 连接器执行的，该连接器由 connector.name 属性在 catalog 中配置。catalog 将数据源中的所有 schema 和表暴露给 Presto

◆ 可以使用 TPC-H 连接器探索 Presto。TPC-H 连接器内置于 Presto 中并提供了一系列的 schema 用于支持 TPC-H

◆ 要配置 TPC-H 连接器，需要创建一个 catalog 属性文件 etc/catalog/tpch.properties，并将连接器配置为 tpch

◆ 每个 catalog 文件都必须有 connector.name 属性。额外的属性由对应 Presto 连接器的实现决定

◆ run 命令将 Presto 启动为前台进程，Presto 的日志和其他输出将写入标准输出流（stdout）和标准错误流（stderr）

◆ 在前台运行 Presto 有助于测试和快速检验进程能否正确启动，以及它是否使用了期望的配置。可以使用 Ctrl-C 组合键结束服务器进程


第 3 章 使用Presto

◆ Presto CLI 提供了一个基于终端的交互式 shell。你可以通过它运行查询并与 Presto 服务端交互来查看元数据

◆ 正如 Presto 服务端一样，Presto CLI 也通过 Maven 中央仓库分发二进制包。这个 CLI 应用程序以可执行 JAR 包的形式提供，你可以像普通 UNIX 可执行程序一样使用它。

◆ 你可以从如下链接查看可用版本的列表：https://repo.maven.apache.org/maven2/io/prestosql/presto-cli

◆ 找到与你使用的 Presto 服务端版本相一致的 CLI 版本，从版本目录中下载对应的 *-executable.jar 文件，然后将其重命名为 presto

◆ 命令行提示符表明你在使用交互式控制台访问 Presto 服务端

◆ CLI 中的大多数命令，尤其是所有 SQL 语句，需要以分号结尾

◆ 要退出 CLI，只需键入 quit 或 exit 命令，或按 Ctrl-D 组合键。

◆ 默认情况下，查询的结果将使用精心配置好的 less 程序分页。你可以通过将环境变量 PRESTO_PAGER 设置为另一个程序的名称（如 more）来覆盖这一行为，或将其置为空来彻底禁用分页

◆ Presto CLI 会保留之前运行过的命令的历史。你可以使用上下箭头键来滚动浏览这些历史，也可以使用 Ctrl-S 组合键和 Ctrl-R 组合键来查询历史。要再次执行一条查询，只需按回车键即可。

◆ 默认情况下，Presto 的命令历史保存在 ~/.presto_history 文件中，你可以使用 PRESTO_HISTORY_FILE 环境变量来修改默认值。

◆ Presto CLI 提供了 --debug 选项来打开运行查询时的调试信息输出。

◆ 使用 Presto CLI 直接执行查询，你需要使用 --execute 选项。注意，在查询语句中你需要使用完全限定符来引用一个表（例如 catalog.schema.table）

◆ 你也可以使用 --catalog 和 --schema 选项来避免使用完全限定符

◆ Presto CLI 提供了 --output-format 选项来控制如何在非交互模式下显示输出，可用的选项有 ALIGNED、VERTICAL、CSV、TSV、CSV_HEADER、TSV_HEADER 和 NULL，默认值是 CSV。

◆ Presto CLI 提供了 --ignore-error 选项，使用它你可以忽略执行文件中的查询时遇到的任何错误。默认行为是在遇到第一个错误时终止执行脚本。

◆ 任何 Java 应用程序都可以通过 Java 数据库连接（JDBC）驱动连接到 Presto。JDBC 是一套标准的数据库访问 API，它提供了在关系数据库中查询、插入、删除和更新数据等的必要方法。许多运行在 JVM 上的客户端应用程序或服务端应用程序使用 JDBC 来访问底层的数据库，以实现数据库管理、报表和其他一些特性。通过 JDBC 驱动，所有这些应用程序都可以使用 Presto

◆ Presto 的 JDBC 驱动允许你连接到 Presto 并使用 SQL 语句与 Presto 交互

◆ 如果你熟悉 JDBC 驱动的不同实现，就知道 Presto 的 JDBC 驱动是 Type 4 驱动，这仅仅意味着它直接与 Presto 原生接口通信。

◆ JDBC 驱动可以让你使用许多强大的 SQL 客户端和数据库管理工具，比如开源工具 DBeaver 或 SQuirreL SQL 客户端等。基于 JDBC 的报告生成、仪表盘和分析工具也可以与 Presto 一起使用

◆ 用这些工具连接 Presto 的步骤都很相似。

◆ 1.  下载 JDBC 驱动。
2.  将 JDBC 驱动放置在应用 classpath 的覆盖范围之内。
3.  配置 JDBC 驱动。
4.  配置 Presto 连接。
5.  连接 Presto 并开始使用。

◆ Presto 的 JDBC 驱动使用 Maven 中央仓库分发，该驱动程序被打包成 JAR 文件。

◆ 你可以从如下链接获得可用版本的列表：https://repo.maven.apache.org/maven2/io/prestosql/presto-jdbc

◆ 要在应用程序中使用 Presto 的 JDBC 驱动，你先要将它加入 Java 应用程序的classpath 中。不同的应用程序有不同的 classpath

◆ 类名称：io.prestosql.jdbc.PrestoDriverJDBC URL 示例：jdbc:presto://host:port/catalog/schema名称：Presto要让驱动工作，只有类名称、JDBC URL 和在 classpath 中的 JAR 包是必填项

◆ 连接到 Presto 的 JDBC URL 采用模板jdbc:presto://host:port/catalog/schema

◆ Presto 服务端提供了一个 Web 界面，通常叫作 Presto Web UI

◆ Presto Web UI 可以借助与 Presto 服务端相同的 HTTP 端口号，使用相同的地址访问。默认端口是 8080，则一个可能的访问地址是http://presto.example.com:8080。因此，在本地的安装环境下，可以在http://localhost:8080 处访问 Web UI。

◆ Presto 是一个兼容 ANSI SQL 的查询引擎，可以让你使用相同的 SQL 语句、函数和运算符来查询和操作所有可以连接到的数据源。

◆ Presto 致力于成为一个与现有 SQL 标准兼容的系统，其主要设计原则之一是既不发明一套新的类 SQL 查询语言，也不偏离 SQL 标准太远。它所有的新功能和语言特性都试图与标准兼容。

◆ Presto 没有将自己绑定在某一个特定的 SQL 标准版本上，相反，它将 SQL 标准看作活动的文档，并且非常重视最新版本的标准。另外，Presto 还未完全实现 SQL 标准所定义的所有必要特性。一条规定是，如果现存的某个功能被发现与标准不兼容，就会被弃用并很快替换为兼容标准的实现

◆ Presto 使你可以使用 SQL 访问外部数据源，如关系数据库、键值存储和对象存储等。

◆ 连接器　　使 Presto 适配一个数据源。每一个 catalog 对应于一个特定的连接器。

◆ catalog　　定义连接到一个数据源的细节。它包含了 schema 并配置了一个连接器来使用。

◆ schema　　组织表的一种方式。catalog 和 schema 一起定义了一个集合的表，这些表可以查询。

◆ 表　　表是无序的行的集合。这些行内容被组织成带有数据类型的有名称的列。

◆ Presto 已可以启动并运行，你可以连接到一个数据源并使用 SQL 来查询它。你能够使用 Presto CLI 或者在其他应用程序中使用 JDBC 来连接 Presto。


```

