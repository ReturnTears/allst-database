# Oracle 11g
```text
username: scott password: 123456
          hyj             123456
          sys             123456
          system          system
```

## Oracle 检索记录
```text
1、WHERE子句会比SELECT子句先执行

```

## Mybatis-flux
```text
创建数据库表
CREATE TABLE tb_account
(
    id        INTEGER PRIMARY KEY ,
    user_name VARCHAR(100),
    age       INTEGER,
    birthday  DATE
);
select * from tb_account ;
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (1, '张三', 18, to_date('2020-01-11', 'yyyy-MM-dd'));
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (2, '李四', 19, to_date('2021-03-21', 'yyyy-MM-dd'));
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (3, '王五', 20, to_date('2022-09-01', 'yyyy-MM-dd'));
INSERT INTO tb_account(id, user_name, age, birthday) VALUES (4, '马六', 21, to_date('2023-02-14', 'yyyy-MM-dd'));

添加 Maven 依赖
<dependencies>
    <dependency>
        <groupId>com.mybatis-flex</groupId>
        <artifactId>mybatis-flex-spring-boot-starter</artifactId>
        <version>1.5.3</version>
    </dependency>
    <dependency>
        <groupId>com.mybatis-flex</groupId>
        <artifactId>mybatis-flex-processor</artifactId>
        <version>1.5.3</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
    </dependency>
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc8</artifactId>
        <version>21.1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.oracle.database.nls</groupId>
        <artifactId>orai18n</artifactId>
        <version>19.7.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.20</version>
    </dependency>
</dependencies>


```

## Oracle 优化
### SQL优化必懂概念
```text
1、基数（CARDINALITY）
某个列唯一键（Distinct_Keys）的数量叫作基数。比如性别列，该列只有男女之分，所以这一列基数是2。主键列的基数等于表的总行数。基数的高低影响列的数据分布。

当查询结果是返回表中5%以内的数据时，应该走索引；当查询结果返回的是超过表中5%的数据时，应该走全表扫描。

select * from TB_ACCOUNT where id=:B1;
“:B1”是绑定变量，可以传入任意值，该查询可能走索引也可能走全表扫描。

2、选择性（SELECTIVITY）
基数与总行数的比值再乘以100%就是某个列的选择性。

什么样的列必须要创建索引呢？当一个列出现在where条件中，该列没有创建索引并且选择性大于20%，那么该列就必须创建索引，从而提升SQL查询性能。

SQL优化核心思想第一个观点：只有大表才会产生性能问题。

抓出必须创建索引的列:
首先，该列必须出现在where条件中，怎么抓出表的哪个列出现在where条件中呢？
有两种方法，一种是可以通过V$SQL_PLAN抓取，另一种是通过下面的脚本抓取。

3、直方图（HISTOGRAM）
如果没有对基数低的列收集直方图统计信息，基于成本的优化器（CBO）会认为该列数据分布是均衡的。

直方图是用来帮助CBO在对基数很低、数据分布不均衡的列进行Rows估算的时候，可以得到更精确的Rows就够了。

什么样的列需要收集直方图呢？当列出现在where条件中，列的选择性小于1%并且该列没有收集过直方图，这样的列就应该收集直方图。

4、回表（TABLE ACCESS BY INDEX ROWID）
当对一个列创建索引之后，索引会包含该列的键值以及键值对应行所在的rowid。
通过索引中记录的rowid访问表中的数据就叫回表。
回表一般是单块读，回表次数太多会严重影响SQL性能，如果回表次数太多，就不应该走索引扫描了，应该直接走全表扫描。

5、集群因子（CLUSTERING FACTOR）
集群因子用于判断索引回表需要消耗的物理I/O次数。
集群因子介于表的块数和表行数之间。
如果集群因子与块数接近，说明表的数据基本上是有序的，而且其顺序基本与索引顺序一样。这样在进行索引范围或者索引全扫描的时候，回表只需要读取少量的数据块就能完成。
如果集群因子与表记录数接近，说明表的数据和索引顺序差异很大，在进行索引范围扫描或者索引全扫描的时候，回表会读取更多的数据块。
集群因子只会影响索引范围扫描（INDEX RANGE SCAN）以及索引全扫描（INDEX FULL SCAN），因为只有这两种索引扫描方式会有大量数据回表。
集群因子不会影响索引唯一扫描（INDEX UNIQUE SCAN），因为索引唯一扫描只返回一条数据。集群因子更不会影响索引快速全扫描（INDEX FAST FULL SCAN），因为索引快速全扫描不回表。

集群因子究竟影响的是什么性能呢？集群因子影响的是索引回表的物理I/O次数。我们假设索引范围扫描返回了1 000行数据，如果buffer cache中没有缓存表的数据块，
假设这1000行数据都在同一个数据块中，那么回表需要耗费的物理I/O就只需要一个；
假设这1000行数据都在不同的数据块中，那么回表就需要耗费1 000个物理I/O。因此，集群因子影响索引回表的物理I/O次数

```

## Oracle数据库锁表
```text
1、锁表原因
可能是修改表中的数据，忘了提交事务会造成锁表。 Oracle数据库操作中，我们有时会用到锁表查询以及解锁和kill进程等操作。

2、锁表查询的代码有以下的形式
select count(*) from v$locked_object;
select * from v$locked_object;

3、查看哪个表被锁
select b.owner,b.object_name,a.session_id,a.locked_mode from v$locked_object a,dba_objects b where b.object_id = a.object_id;
OWNER ：数据表的所有者用户
OBJECT_NAME： 被锁住的表名
SESSION_ID： 会话ID
LOCKED_MODE： 锁级别

锁级别分为6级：
1级锁有：Select 
2级锁有：Select for update,Lock For Update,Lock Row Share
3级锁有：Insert, Update, Delete, Lock Row Exclusive
4级锁有：Create Index,Lock Share
5级锁有：Lock Share Row Exclusive
6级锁有：Alter table, Drop table, Drop Index, Truncate table, Lock Exclusive

4、查看是哪个session引起的
SELECT a.OS_USER_NAME, c.owner, c.object_name, b.sid, b.serial#,logon_time
FROM v$locked_object a, v$session b, dba_objects c
WHERE a.session_id = b.sid
AND a.object_id = c.object_id
ORDER BY b.logon_time

5、杀掉对应进程
alter system kill session '1025,41';
需要用户有管理员的权限操作，其中1025为sid,41为serial#

如果有ora-00031错误，则在后面加immediate;
alter system kill session '1025,41' immediate;

6、如何避免锁表
常见问题是用户更新操作没有提交事务，
所以：如果单独更新操作，需要写2个操作 SQL，一个是更新操作SQL语句，另一个是commit语句提交事务。

```