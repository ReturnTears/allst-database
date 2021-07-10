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