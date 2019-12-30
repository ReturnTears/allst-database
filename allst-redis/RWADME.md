# Redis

## String应用场景

**单值缓存**
```
SET key value
GET key

```

**对象缓存**
```
SET user:1 value(json格式数据)
MSET user:1:name zhangsan user:1:age 22
MGET user:1:name user:1:age
```

**分布式锁**
```
SETNX product:101 true
SETNX product:102 true

DEL product:101

SET product:101 true ex 10 nx
```

**计数器**
```
INCR article:read:page:{文章id}
GET article:read:page:{文章id}
```

**Web集群session共享**
```
spring session + redis实现session共享
```

**分布式系统全局序列号**
```
INCRBY orderId 1000     redis批量生成序列号提升性能
```

## Hash常用操作
****
```
HSET key field value
HSETNX key field value
HMSET key field value
HGET key field value
HMGET key field value
HDEL key field value
HLEN key
HGETALL key

HINCRBY key field increment
```

## List常用操作
```
LPUSH key value [value...]
RPUSH key value [value...]
LPOP key
RPOP key
LRANGE key start stop
BLPOP key [key...] timeout
BRPOP key [key...] timeout

```
**常用数据结构**
```
Stack(栈) = LPUSH + LPOP   FILO
QUEUE(队列) = LPUSH + RPOP
Blocking MQ(阻塞队列) =  LPUSH + BLPOP              --- key timeout

```

## SET
```
SADD key member [member...]
SREM key member [member...]
SMEMBERS key
SCARD key
SISMEMBER key member
SRANDMEMBER key [count]
SPOP key [count]

```

## 

# USE
```
https://myusf.usfca.edu/arts-sciences/computer-science
https://www.bilibili.com/video/av62657941?p=5
http://redisdoc.com/
```