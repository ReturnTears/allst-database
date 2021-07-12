# Redis 6.2.4
```text
Redis是单线程+多路IO复用
select db_number
flushdb
flushall

del key
unlink key 根value选择非阻塞删除，仅将keys从keyspace元数据中删除， 真正的删除会在后续异步操作
expire key 10  为给定的key设置10秒过期时间
ttl key 查看过期状态，-1表示永不过期，-2表示已过期

String类型是二进制安全的，value值最大可以存储512MB
incr 是原子操作
msetnx 是原子操作

redis发布与订阅
redis客户端可以订阅任意数量的频道
订阅频道：
	subscribe channel0（频道名称）
给频道发布消息：
	publish channel0（频道名称） helloworld（消息内容）
	
Redis新数据类型
Bitmaps
合理使用操作位能够有效地提高内存使用率和开发效率
setbit key offset value
getbit key
bitcount key
bitop and | or | not 

hyperLogLog

Geospatial

Redis事务:
Redis事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
Redis事务的主要作用就是串联多个命令防止别的命令插队。
从输入Multi命令开始，输入的命令都会依次进入命令队列中，但不会执行，直到输入Exec后，Redis会将之前的命令队列中的命令依次执行。
组队的过程中可以通过discard来放弃组队。
Multi、Exec、discard
multi 开启事务
exec 执行事务
discard 放弃执行事务

事务错误处理:
组队中某个命令出现了报告错误，执行时整个的所有队列都会被取消。
如果执行阶段某个命令报出了错误，则只有报错的命令不会被执行，而其他的命令都会执行，不会回滚。


dump.rdb文件的生成规则:
默认生成在:  dir ./ 由该配置项决定
例如：启动redis时使用命令：
       cd /usr/local/redis/ 在该目录下运行redis:
       src/redis-server redis.conf
     就会在/usr/local/redis/目录下生成dump.rdb文件
    
    启动redis时使用命令：
       cd /usr/local/redis/src 在该目录下运行redis:
       redis-server redis.conf
     就会在/usr/local/redis/src目录下生成dump.rdb文件

查看redis进程信息：
ps -ef | grep redis
kill -9 pid


redis 6.2 默认持久化规则(在指定时间间隔内将内存中的数据集快照写入磁盘):
save 3600 1         # 3600s 1个key发生操作
save 300 100        # 300s 100个key发生操作
save 60 10000       # 60s 1w个key发生操作

vi //usr/local/redis/redis.conf
::set nu 打开配置文件redis.conf的行号

主从复制
info replication ： 打印主从复制的相关信息

1、一主二仆
   slaveof <ip> <port> 成为某个实例的从服务器，ip与port为主库的地址和端口信息。配从库不配主库。
   在主机上写，在从机上可以读取数据，在从机上写数据报错
   主机挂掉，重启就行，一切如初
   从机重启需重设：slaveof <ip> <port> ，也可以将配置信息添加到配置文件中。永久生效。

   
2、薪火相传
   上一个Slave可以是下一个slave的Master，Slave同样可以接收其他 slaves的连接和同步请求，那么该slave作为了链条中下一个的master, 可以有效减轻master的写压力,去中心化降低风险。
   用 slaveof <ip> <port>
   中途变更转向:会清除之前的数据，重新建立拷贝最新的
   风险是一旦某个slave宕机，后面的slave都没法备份
   主机挂了，从机还是从机，无法写数据了

3、反客为主
   在薪火相传的模式中，当一个master宕机后，后面的slave可以立刻升为master，其后面的slave不用做任何修改。
   用 slaveof no one 将从机变为主机。

4、哨兵模式
   slave-priority 10 ： 设置从机的优先级，值越小，优先级越高，用于选举主机时使用。默认100。（老版本中的配置）
   replica-priority 10 ： 同上， 新版本中的配置
   sentinel.conf中配置如下命令行：
   sentinel monitor shizhan 192.168.33.100 6379 1
   其中shizhan为监控对象起的服务器名称， 1 为至少有多少个哨兵同意迁移的数量
   启动哨兵模式：
   src/redis-sentinel sentinel.conf

Redis集群
代理主机
redis3.0+ 开始使用的是无中心化集群配置
Redis 集群实现了对Redis的水平扩容，即启动N个redis节点，将整个数据库分布存储在这N个节点中，每个节点存储总数据的1/N。
Redis 集群通过分区（partition）来提供一定程度的可用性（availability）： 即使集群中有一部分节点失效或者无法进行通讯， 集群也可以继续处理命令请求。

集群配置需删除持久化文件: dump.rdb,appendonly.aof
由于条件受限，采用单台虚拟机模拟集群的方式，分别开启6个实例：6379、6380、6381、6389、6390、6391
实例的基本配置信息如下：
redis.conf配置文件==>>
    开启 daemonize yes
    pid 文件名字
    指定端口
    log文件名字
    dump.rdb名字
    appendonly no
实例的集群配置如下：
在redis安装目录中新建目录myConf、redis_cluster
在myConf目录下新建配置文件redis6379.conf==>
include /usr/local/redis/myConf/redis.conf
port 6379
pidfile "/var/run/redis_6379.pid"
dbfilename "dump6379.rdb"
dir "/usr/local/redis/redis_cluster"
logfile "/usr/local/redis/redis_cluster/redis_err_6379.log"
cluster-enabled yes  # 打开集群模式
cluster-config-file nodes-6379.conf # 设定节点配置文件名
cluster-node-timeout 15000  # 设定节点失联时间，超过该时间（毫秒），集群自动进行主从切换。
复制redis6379.conf文件修改为剩余的实例配置文件采用如下命令依次替换即可：
替换文本中字符：:%s/查询文本/替换文本
例如： :%s/hello/hi 将hello替换为hi

启动6个实例（服务），使用如下命令依次启动即可：
src/redis-server myConf/redis6379.conf

查看redis集群进程信息
ps -ef|grep redis

将6个节点合成一个集群
确保redis实例都启动，且节点nodes-xxx.conf配置文件都正常生成
使用如下命令将6个节点合并为一个集群：
src/redis-cli --cluster create --cluster-replicas 1 192.168.33.100:6379 192.168.33.100:6380 192.168.33.100:6381 192.168.33.100:6389 192.168.33.100:6390 192.168.33.100:6391
此处不要使用127.0.0.0,需要使用服务器真实的IP地址
--replicas 1 采用最简单的方式配置集群，一台主机，一台从机，正好三组

集群方式登录：
src/redis-cli -c -h 192.168.33.100 -p 6379 （我是采用Windows下连接的redis集群服务， 若在centos本机下可用不用-h参数）
-c 采用集群策略连接，设置数据会自动切换到相应的写主机
通过 cluster nodes 命令查看集群信息

什么是slots ?
一个 Redis 集群包含 16384 个插槽（hash slot）， 数据库中的每个键都属于这 16384 个插槽的其中一个， 
集群使用公式 CRC16(key) % 16384 来计算键 key 属于哪个槽， 其中 CRC16(key) 语句用于计算键 key 的 CRC16 校验和 。
集群中的每个节点负责处理一部分插槽。 举个例子， 如果一个集群可以有主节点， 其中：
节点 A 负责处理 0 号至 5460 号插槽。
节点 B 负责处理 5461 号至 10922 号插槽。
节点 C 负责处理 10923 号至 16383 号插槽。
不在一个slot下的键值，是不能使用mget,mset等多键操作。
mset k1 v1 k2 v1 k3 v3 使用该命令会报错
可以通过{}来定义组的概念，从而使key中{}内相同内容的键值对放到一个slot中去。
mset k1{cust} v1 k2{cust} v1 k3{cust} v3

查看集群中的值：
CLUSTER GETKEYSINSLOT <slot><count> 返回 count 个 slot 槽中的键:
cluster keyslot cust
cluster countkeysinslot 上一个命令结果 keyslot （插槽key）
cluster getkeysinslot keyslot 10

故障恢复：
如果主节点下线，从节点能否自动升为主节点？ 是的。注意：15秒超时
主节点恢复后，主从关系会如何？主节点回来变成从机。
果所有某一段插槽的主从节点都宕掉，redis服务是否还能继续?
如果某一段插槽的主从都挂掉，而cluster-require-full-coverage 为yes ，那么 ，整个集群都挂掉
如果某一段插槽的主从都挂掉，而cluster-require-full-coverage 为no ，那么，该插槽数据全都不能使用，也无法存储。
redis.conf中的参数  cluster-require-full-coverage

Redis集群优点：
1、实现扩容
2、分摊压力
3、无中心化配置，相对简单
Redis集群缺点：
1、多键操作是不被支持的
2、多键的Redis事务是不被支持的，Lua脚本不被支持
3、由于集群方案出现较晚，很多公司已经采用了其他的集群方案，而代理或者客户端分片的方案想要迁移至redis cluster，需要整体迁移而不是逐步过渡，复杂度较大。

Redis应用问题解决
1、缓存穿透
   key对应的数据在数据源并不存在，每次针对此key的请求从缓存获取不到，请求都会压到数据源，从而可能压垮数据源。
   eg:比如用一个不存在的用户id获取用户信息，不论缓存还是数据库都没有，若黑客利用此漏洞进行攻击可能压垮数据库。
解决方案：
   1、对空值缓存：如果一个查询返回的数据为空（不管是数据是否不存在），我们仍然把这个空结果（null）进行缓存，设置空结果的过期时间会很短，最长不超过五分钟
   2、设置可访问的名单（白名单）：使用bitmaps类型定义一个可以访问的名单，名单id设置为bitmaps的偏移量，每次访问和bitmap里面的id进行比较，如果访问id不在bitmaps里面，进行拦截，不允许访问。
   3、采用布隆过滤器：布隆过滤器可以用于检索一个元素是否在一个集合中。它的优点是空间效率和查询时间都远远超过一般的算法，缺点是有一定的误识别率和删除困难。
                   将所有可能存在的数据哈希到一个足够大的bitmaps中，一个一定不存在的数据会被 这个bitmaps拦截掉，从而避免了对底层存储系统的查询压力。
   4、进行实时监控：当发现Redis的命中率开始急速降低，需要排查访问对象和访问的数据，和运维人员配合，可以设置黑名单限制服务。

2、缓存击穿
   key对应的数据存在，但在redis中过期，此时若有大量并发请求过来，这些请求发现缓存过期一般都会从后端DB加载数据并回设到缓存，这个时候大并发的请求可能会瞬间把后端DB压垮。
解决方案：
    （1）预先设置热门数据：在redis高峰访问之前，把一些热门数据提前存入到redis里面，加大这些热门数据key的时长
    （2）实时调整：现场监控哪些数据热门，实时调整key的过期时长
    （3）使用锁：
        （1）就是在缓存失效的时候（判断拿出来的值为空），不是立即去load db。
        （2）先使用缓存工具的某些带成功操作返回值的操作（比如Redis的SETNX）去set一个mutex key （互斥锁）
        （3）当操作返回成功时，再进行load db的操作，并回设缓存,最后删除mutex key
        （4）当操作返回失败，证明有线程在load db，当前线程睡眠一段时间再重试整个get缓存的方法

3、缓存雪崩
   缓存雪崩的问题描述和缓存击穿一致。
   缓存雪崩与缓存击穿的区别在于这里针对很多key缓存，前者则是某一个key。
缓存失效时的雪崩效应对底层系统的冲击非常可怕
解决方案：
    （1）构建多级缓存架构：nginx缓存 + redis缓存 + 其他缓存（ehcache等）
    （2）使用锁或队列：用加锁或者队列的方式来保证不会有大量的线程对数据库一次性进行读写，从而避免失效时大量的并发请求落到底层存储系统上。不适用高并发情况
    （3）设置过期标志更新缓存：记录缓存数据是否过期（设置提前量），如果过期会触发通知另外的线程在后台去更新实际key的缓存。
    （4）将缓存失效时间分散开：比如我们可以在原有的失效时间基础上增加一个随机值，比如1-5分钟随机，这样每一个缓存的过期时间的重复率就会降低，就很难引发集体失效的事件。

    
```

# 案例
```text
case-1: 验证码
需求
输入手机号，点击后发送随机6位数验证码，2分钟有效，验证是否有效，每个手机号每天只能输入3次
解析
1、生成6位随机数Random
2、验证码放入到Redis中设置过期时间120s
3、验证是否一致，从redis获取验证码和输入的验证码进行比较
4、每个手机号只能发送3次验证码，使用incr，>2时提交不能发送

case-2: 模拟转账，处理事务冲突问题

case-3: 秒杀
模拟秒杀后端逻辑：
                    Key             Value
    商品库存 =》 sk:prodId:qt     (String)剩余数量   ➖减数量
秒杀成功者清单 =》sk:prodId:user   (set) 成功者id1,成功者id2..... ＋增加人数
存在问题：
1、连接超时问题
    连接池
2、超卖问题
    乐观锁：watch监视、事务、组队
3、乐观锁造成库存遗留问题
   当某个用户提交数据并修改了版本号后，其他用户正常提交的数据由于版本号不一致也不能进行数据的操作
   使用Lua脚本

添加：
CentOS7通过ab并发测试
vim postfile 模拟表单提交参数,以&符号结尾;存放当前目录。
内容：prodId=0101&
ab -n 2000 -c 200 -k -p ~/postfile -T application/x-www-form-urlencoded http://192.168.33.100:4568/sec/dt
 
```