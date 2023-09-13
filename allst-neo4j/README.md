# Neo4j

## 知识图谱

## 图数据库
```text
图数据库(Graph database)指的是以图数据结构的形式来存储和查询数据的数据库。

```

## neo4j
```text
Neo4j构建模块


Linux下neo4j环境搭建
1、下载地址：
https://neo4j.com/artifact.php?name=neo4j-community-3.5.17-unix.tar.gz
2、解压
tar -xvf neo4j-community-3.5.17.tar
3、修改配置文件 neo4j.conf
vi conf/neo4j.conf
主要是修改 允许远程访问的地址 把对应的注释打开即可
dbms.connectors.default_listen_address=0.0.0.0
4、开放对应的访问端口 默认要开放7474 和 7687
firewall-cmd --zone=public --add-port=7474/tcp --permanent
firewall-cmd --zone=public --add-port=7687/tcp --permanent
systemctl reload firewalld
5、neo4j启动命令
./bin/neo4j start
6、使用浏览器 访问服务器上的 neo4j
http://ip:7474/
我这里的ip：192.168.0.108
默认的账号是 neo4j 密码 neo4j 这里第一次登录的话会要求修改密
第一次访问时需要登录用户名和密码：都为neo4j, 需要修改密码：我这里修改为：hadoop

Windows下neo4j环境搭建：
1、下载地址：
https://neo4j.com/download-center/#community
下载最新的Neo4j Server安装文件、可以看到 neo4J 软件 exe 或 zip 格式的所有版本
2、解压
我下载版本为：neo4j-community-4.3.6-windows.zip
3、修改配置文件conf/neo4j.conf
dbms.connectors.default_listen_address=0.0.0.0
4、通过 neo4j.bat install-service 安装neo4j服务
注意的问题: 如果是4.0 以及以上版本需要jdk11
修改文件 bin/neo4j.ps1
Import-Module "neo4j的主目录\bin\Neo4j-Management.psd1"
如果配置了环境变量，上述修改文件步骤则不用执行。
5、neo4j.bat启动
neo4j.bat start
6、使用浏览器 访问服务器上的 neo4j
http://127.0.0.1:7474
默认的账号是 neo4j 密码 neo4j 这里第一次登录的话会要求修改密码
修改后：username:neo4j,password:hadoop
7、配置Neo4j允许远程访问
放开注释#dbms.connector.http.listen_address=:7474
远程bolt连接：
放开注释#dbms.connector.bolt.listen_address=:7687
取消认证机制：dbms.security.auth_enabled=false

Neo4j的bin目录中还提供了一个cypher-shell.bat程序，它相当于Neo4j客户端。打开命令行窗口，输入如下命令
cypher-shell -a localhost:7687 -u neo4j -p hadoop -d neo4j
该命令中的-a指定Neo4j服务器地址，-u指定登录所使用的用户名，-p指定密码，-d指定默认连接的数据库

```

## Cypher与CQL
```text
Cypher
是neo4j采用的查询语言，是一种
1、描述性图查询语言；
2、尝试用简单方式表达复杂查询过程的语言；
3、被设计为简单但高效的语言；
4、Cypher的设计受到SQL、SPARQL等语言的启发；

CQL
什么是CQL
百度翻译的结果称CQL为持续性查询语言；

```

## COL
```text
SHOW DATABASES ;
//SHOW DATABASE neo4j ;
//:use neo4j ;
MATCH (b: Book{price: 148}) RETURN b;
MATCH (b) RETURN b;
match(b:Book) where b.price > 120 return b;
match(b:Book) return b order by b.price;
match(b) return b order by coalesce(b.price, 0);
match(b) return b order by coalesce(b.price, 0) SKIP 4 LIMIT 2;
MATCH(n) where id(n) in [0, 2] RETURN n;
OPTIONAL MATCH(n{price:50}) RETURN n;
CREATE (:Book {name: "Go中级讲义"}) - [:WRITTEN_BY] -> (:Author);
MATCH(n) WHERE id(n) > 7 RETURN n;
CREATE (n: Student {name: "孙悟空"}) <- [r:TEACHING {addr:"灵台方寸山"}] - (m:Teacher {name: "菩提"}) RETURN r;
MATCH (n{name: "孙悟空"}), (m{name: "菩提"}) CREATE (n)-[:LEARNING]->(m) ;
MATCH (n), (m) where id(n) = 24 and id(m) = 25 CREATE (n)-[:EVICT]->(m);
MATCH ()-[r]-() RETURN r;
MATCH ()-[r:WRITTEN_BY]-() RETURN r;
MATCH (n:Student)-[r]-(t:Teacher) RETURN n, r;
MATCH (n:Author) <- [r] -> () RETURN id(n), n, r;
MATCH (n)-[r]-() WHERE id(r) = 0 RETURN id(n), n, r;
MATCH (n)-[r]->(m) where id(endNode(r)) = 9 RETURN r;
MATCH (n: Teacher) <--> (m) RETURN n, m;
MATCH (n: Teacher) <--> (m) RETURN DISTINCT n, m;
MATCH (n) WHERE id(n) = 9 CREATE (b:Book {name: "SpringBoot终极讲义"})-[:WRITTEN_BY]->(n),(b)<-[:USING]-(n);
MATCH (n:Author) <--> (m) RETURN n, m;
MATCH () <-[r:USING]-> () RETURN r ;
MATCH () <-[r:USING]-> () DELETE r ;
MATCH (n {name: "SpringBoot终极讲义"}) <- [r] -> (m) DELETE r RETURN n, r;
MATCH (n {name: "SpringBoot终极讲义"}) DETACH DELETE n RETURN n;
MATCH (n) WHERE id(n) = 2 REMOVE n: Teacher RETURN n;
MATCH (n {name: "孙悟空"}) <-[r: TEACHING]->() REMOVE r.addr RETURN r;
MATCH (n) WHERE id(n) = 3 REMOVE n.age, n : Teacher RETURN n;
MATCH (n: Scala), (m) where id(m) = 0 REMOVE n.price DELETE m RETURN n;
MATCH (n : Python) SET n.price = 118 RETURN id(n), n ;
MATCH (n : Scala) SET n.price = 118 RETURN id(n), n ;
MATCH (n : Scala) WHERE id(n) = 5 SET n.price = 128 RETURN id(n), n ;
MATCH (n) where id(n) = 3 set n.age = 25, n: Teacher RETURN n;
MATCH (n {name: "孙悟空"}) <-[r: TEACHING]-() SET r.addr = "灵台方寸山", r.time = "500 year age" RETURN r;
MATCH (n: Book) RETURN n.name UNION MATCH (n: Author) RETURN n.name ;
MATCH (n: Book) RETURN n.name UNION ALL MATCH (n: Author) RETURN n.name ;
MATCH (n: Book) RETURN n.name, n.price as price UNION MATCH (n: Author) RETURN n.name, n.age as price ;
CREATE INDEX book_name_index IF NOT EXISTS for(n :Book) on (n.name) ;
MATCH (n:Person) where n.name = "KangKang" RETURN n;
MATCH (b: Book) USING INDEX b : Book(name) WHERE b.name = "SpringBoot终极讲义" RETURN b;
CREATE INDEX IF NOT EXISTS for(n :Book) on (n.name, n.price) ;
DROP INDEX book_name_index IF EXISTS ;
FOREACH (value in ["wusong","jinlian","linchong","luda"] | CREATE (:Person {name: value})) ;
MATCH p = (n)-[*]->(m) FOREACH (n in nodes(p) | set n.tag = "new Label") ;
MATCH (n{tag: "new Label"}) RETURN n ;
MATCH p = (n)-[*]->(m) FOREACH (n in nodes(p) | REMOVE n.tag) ;
UNWIND [1,2,3, NULL] as x RETURN x, "fkjava" as y;
UNWIND [1,2,3,2,1] as x WITH DISTINCT x RETURN collect(x) as newList;
UNWIND [[1,2],["a","b"],"c"] as x UNWIND x as y RETURN y;
```

## 语法基础
```text
数据类型
类型	     用法
boolean	true;false
byte	用于表示8位整数
short	用于表示16位整数
int	    用于表示32位整数
long	用于表示64位整数
float	用于表示32位浮点数
double	用于表示64位浮点数
char	用于表示16位字符
String	用于表示字符串


```