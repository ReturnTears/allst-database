# ODPS基本概念

## 什么是ODPS？
开发数据处理服务(Open Data Processing Service，简称ODPS)，2016年后更名MaxComputer。
ODPS是一种由阿里云自主研发，针对TB/PB级数据、实时性要求不高的分布式处理服务。
主要服务于批量结构化数据的存储和计算，可以提供海量数据仓库的解决方案以及针对大数据的分析建模服务。

## ODPS的组成对象
### 项目空间(Project)
项目空间是MaxComputer 的基本组织单元，它类似于传统数据库中的Database或者Schema的概念，是进行多用户隔离和访问控制的主要边界。
一个用户可以同时拥有多个项目空间的权限。通过安全授权，可以在一个项目空间中访问另一个项目空间中的对象。

### 表(Table)
表是MaxCompute的数据存储单元。它在逻辑上也是由行和列组成的二维结构，每行代表一条记录，每列表示相同数据类型的一个字段，
一条记录可以包含一个或者多个列，各个列的名称和类型构成这张表的Schema。
MaxComputer的表格分为两种类型：外部表及内部表。
内部表的所有数据都被存储在MaxComputer中。表中的列可以是MaxCompute支持的任意数据类型(Bigint、Double、String、 Boolean和Datetime)。
MaxCompute中的各种类型计算任务(输入、输出)的操作对象都是表。用户可以创建、删除表以及向表中导入数据。
对于外部表，MaxCompute并不真正持有数据，表格的数据可以存放在OSS中。MaxCompute仅会记录表格中的Meta信息。
用户可以通过MaxCompute的外部表机制处理OSS上的非结构化数据，例如：视频、音频、基因、气象、地理信息等。其主要流程包括：
* 将数据上传至OSS；
* 在RAM产品中授予MaxCompute服务读取OSS数据权限。
* 自定义Extractor：用户读取OSS上特殊格式数据。目前，MaxCompute默认提供CSV格式的Extractor，并提供视频格式数据读取的代码样例。
* 创建外部表；
* 执行SQL作业分析数据；
 
**注意**：目前MaxCompute仅支持读取外部数据，即读取OSS数据，不支持向外部写入数据；

### 分区(Partition)
分区表指的是在创建表时指定的分区空间，即指定表内的某几个字段作为分区列。大多数情况下，用户可以将分区类比为文件系统下的目录。
MaxCompute将分区列每一个值作分区(目录)。用户可以指定多级分区，即将表的多个字段作为表的分区，分区之间正如多级目录的关系。
在使用数据时如果指定了需要访问的分区名称，则只会读取相应的分区，避免扫描全表，提高处理效率，降低费用。
```sql
create table src (key string, value bigint) partitioned by (pt string);     
```
目前，MaxCompute仅承诺String分区。且目前最多支持六级分区

### 自定义函数(User Defined Functuon，简称UDF)
MaxCompute为用户提供了SQL计算功能，用户可以在MaxCompute SQL中使用系统的内建函数完成一定的计算和计数功能。
但是当内建函数无法满足要求时，用户可以使用MaxCompute提供的Java编程接口开发自定义函数UDF，
UDF又可以进一步分为标量值函数UDF、自定义聚合函数UDAF和自定义表值函数UDTF三种。

### 资源(Resource)
资源是MaxCompute的特有概念。用户如果想使用MaxCompute的自定义函数(UDF)或者MapReduce功能需要依赖资源来完成。
例如用户在编写好UDF后，需要将编译好的jar包以资源的形式上传到ODPS。运行这个UDF时，MaxCompute会自动下载这个Jar包，
获取用户代码，运行UDF而无需用户干预。上传Jar包的过程就是在MaxCompute上创建资源的过程。

### 任务(Task)和作业(Job)
任务是ODPS的基本计数单元。SQL以及MapReduce功能都是通过任务完成的。
对于用户提交的大多数任务，特别是计算型任务，MaxCompute会将其进行解析，得出任务的执行计划。
执行计划是由具有依赖关系的多个执行阶段(Stage)构成的。目前，执行计划逻辑上可以被看作一个有向图，
图中的点是各个执行阶段，边是各个执行阶段之间的依赖关系。在同一个执行阶段内，会有多个进程，也称之为Worker，
共同完成该执行阶段的计算工作。同一个执行阶段内的不同Worker之间只是处理的数据不同，执行逻辑完成相同。
作业(Job)是由一个或者多个Task以及表示其执行次序关系的工作流(Workflow)，工作流是个有向无环图。
当一个作业被提交到系统中执行时，该作业就会拥有一个作业实例(Instance)。另一方面，部分MaxCompute任务并不是计算型任务。
例如DDL SQL语句，这些任务本质上只需要读取修改MaxCompute的元数据，因此这些任务不能被解析出执行计划。

### 工作流
工作流是一个DAG图（有向无环图），其描述了作业中多个节点之间的逻辑（依赖关系）和规则（运行约束）。

### 节点
节点属于工作流的子对象，也称为任务，是大数据开发平台数据处理和分析过程最基本单元，每个任务对应DAG图中的一个节点，
其可以是一个SQL Query、命令和MapReduce程序。

### 依赖关系
依赖关系是描述两个或多个节点/工作流之间的语义连接关系，其中上游节点/工作流运行可以影响下流节点/工作流的运行状态，反之则不成立。

### 实例
在阿里与大数据开发平台中，节点任务在执行时会被实例化，并以ODPS实例的方式存在。实例会经历未运行、等待时间/等待资源、运行中、成功/失败几个状态。


## ODPS的基础构架
MaxCompute构架分为四层，分别是客户端、接入层、逻辑层和计算层：
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/架构图.png)

### 【客户端】
ODPS以RESTful API方式对外提供服务，用户可以通过不同的方式来使用ODPS的服务，
包括直接通过RESTful API请求访问、ODPS SDK、ODPS CLT(Command Line Tool)、Java集成开发环境和管理控制台等。
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/客户端.png)

### 【接入层】
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/接入层.png)

### 【逻辑层】
逻辑层又称为控制层，是ODPS的核心部分。可以认为是ODPS的大脑，负责项目空间、对象管理、授权管理、命令解析、元数据五部分。
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/逻辑层.png)
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/逻辑层组件.png)

### 请求处理器(Worker)
负责处理所有RESTful请求，本地处理一些作业，提交分布式作业给调度器。
本地能处理的作业包括：用户空间、表、资源、任务等的管理。
需要提交给调度器的作业包括：SQL、MR等分布式计算的任务。
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/worker.png)


### 调度器（Scheduler）
调度器负责Instance调度以及查询计算集群的资源情况。
处理Instance的任务包括：
+ 维护一个Instance列表。
+ 把Instance分解成Task
+ 生成Task的工作流（DAG 图）
+ 把可运行的Task放到TaskPool中
+ 定时对该优先级队列进行排序。
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/schedulerf.png)


### 作业执行管理器（Executor）
负责向TaskPool申请Task，生成任务描述文件提交给计算层，监控并反馈状态给调度器。
作业执行器的运行细节：
判断自身资源是否充足。
主动轮询TaskPool,请求下一个Task，生成计算层的分布式作业描述文件，提交给计算层。
监控这些任务的运行状态，定时把状态汇报给调度器。
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/executor.png)


【计算层】
计算层开始真正执行计算任务。
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/计算层.png)


Pangu(盘古)文件系统存储文件的格式如下：
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/存储文件格式.png)
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/元数据管理.png)


一条ODPS SQL的执行
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/提交作业.png)
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/运行作业.png)
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/查询状态.png)


## 权限管理
**角色隔离**
#### 组织管理员
指组织的管理者，可新建计算引擎、新建项目空间、新建调度资源、添加组织成员、为组织成员赋予组织管理员角色、配置数据类目等。

### 项目管理员
指项目空间的管理者，可针对项目空间基本属性、数据源、当前项目空间计算引擎配置和项目成员进行管理。并为组织成员赋予项目管理员、开发、运维、部署、访客角色。

### 开发
开发角色用户能够创建工作流、脚本文件、资源UDF、新建表，同时可以创建发布包，但不能执行发布操作。

### 运维
项目空间但运维人员，由项目管理员/项目所有者分配运维权限；拥有发布及线上运维的操作权限，但无数据开发的操作权限。

### 部署
部署角色与运维角色相似，但是其没有线上运维操作权限。

### 访客
访客角色的用户只具备查看权限，而无权限进行编辑工作流和代码等。

## 角色管理
角色(role)是一组访问权限的集合。
Owner：当一个用户创建了一个项目，他便自动成为该项目的Owner。任何没有被项目的Owner授权的人都无法访问该项目。
如果Alice创建了一个项目WonderLand，然后她要授权Bob访问该项目的一些对象，那么：
首先，Bob要有一个合法的云账号。
其次，Alice要把Bob的云账号加到项目中来。
最后，赋予一些对象的权限给Bob。
当Alice要禁止Bob访问该项目时，则直接将其云账号从项目中移除即可，前提是Bob没有被赋予任何角色。
值得注意的是，Bob虽然被移除来项目，但他之前被授予但权限仍保留在项目中。一旦被Alice再次加入该项目，原有权限会被自动激活。
缺省角色(Admin)：拥有该角色权限的用户，可以访问项目空间中所有对象，不能设定项目空间的安全配置和修改项目空间的鉴权模型；能进行用户与角色管理，但是不能修改将Admin角色的权限或将Admin角色赋给别的用户，只能删除没有被使用的角色。

## 授权
授权有主体(Subject)、客体(Object)和操作(Action)三要素。
授权有ACL(基于对象的授权)和Policy(基于策略的授权)两种方法。
![Image](https://github.com/ReturnTears/allst-db/allst-odps/blob/master/src/main/resources/static/picture/授权.png)


查看权限
```sql
【查看当前用户的权限】
show grants;
【查看指定用户的权限】
show grants for <username>;
【查看指定角色的权限】
describe role <rolename>;
【查看指定对象的授权】
show acl for <objectName>[on type <objecttype>];
```

ACL授权的语法
```sql
GRANT <privileges> ON <object> TO <subject>;
REVOKE <privileges> ON <object> FROM <subject>;
```

### 数据保护机制
设置ProjectProtection:数据只能流入，不能流出。默认时，ProjectProtection不能被设置，需要手工开启。
```text
ProjectProtection=true
```
如何在项目保护状态下，进行合规的数据流出操作。
办法1：在设置项目保护(ProjectProtection)的同时，附加一个例外策略(exception)
```text
set ProjectProtection=true exception <policyFile>;
```
办法2：将两个相关的项目空间设置为互信(TrustedProject)，则数据的流向将不会被视为违规。
```text
add trustedproject=SecretGarden;
```

### 项目空间的鉴权模型
ODPS支持多种正交的授权机制，用户可通过设置下列参数来定制项目空间的鉴权模型。
security.CheckPermissionUsingACL --> 激活/冻结ACL授权机制，默认为True
security.CheckPermissionUsingPolicy --> 激活/冻结Policy授权机制，默认为True
security.ObjectCreatorHasAccessPermission -->允许/禁止对象创建者默认拥有访问权限，默认为True。
security.ObjectCreatorHasGrantPermission --> 允许/禁止对象创建者默认拥有授权权限，默认为True。
security.LabelSecurity --> 开启/关闭LabelSecurity安全策略，默认为False。
ProjectProtection -->开启，关闭项目的数据空间保护机制，默认为False。
```sql
【查看当前鉴权模型】
show SecurityConfiguration;
```

### 数据的敏感等级分类
Project Owner需要定义明确的数据敏感等级和访问许可等级划分标准，默认时所有用户的访问许可等级为0级，数据安全默认等级默认为0级。
LabelSecurity对敏感数据的粒度可以支持列级别，管理员可以对表的任何列设置敏感度标记(Label)，一张表可以由不同等级的敏感数据列所组成。
LabelSecurity基本操作
```sql
【打开LabelSecurity安全机制开关】
set security.LabelSecurity=<true|false>;
【设置用户安全许可标签】
set label<number> to user<username>;
【设置数据敏感等级标签】
set label<number> to table <tablename[(column_list)]>;
【显示授权低级别用户访问高敏感数据】
grant label <number> on table <tablename>[(column_list)] to user <username>[with exp <days>];
【撤销显示授权】
revoke label on table <tablename>[(column_list)] from user<username>;
【清洗过期的显示授权】
clear expired grants;
【查看一个用户能访问哪些敏感数据级】
show label[<level>] grants [for user <username>];
【查看一个敏感数据表能被哪些用户访问】
show label [<level>] grants on table <tablename>;
【用户对指定表上列级别的Label授权】
show label [<level>] grants on table <tablename> for user <username>;
【包安装者对包中敏感资源许可访问级别】
allow project <pjname> to install package <pkname>[using label <n>];
```
1.LabelSecurity安全机制开关必须由owner打开，admin角色没有此权限。
2.用户的安全许可标签和文件敏感等级取值均为0到9，两者相互对应。
3.显示设置的列的敏感等级优先级高于表的敏感等级，和顺序、等级高低无关。
4.设置包时，若省略[using label<number>]，则默认级别为0级，即只可以访问非敏感数据。
5.跨项目访问敏感数据时，包安装者的项目空间中的所有用户都将使用此许可的访问级别。
```sql
set label 1 to label t1;
set label 2 to table t1(id,name);
set label 3 to table t1;
```



