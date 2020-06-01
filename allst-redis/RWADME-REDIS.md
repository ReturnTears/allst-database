# Redis

## String应用场景

**单值缓存**
```
缓存：缓存数据、缓存绘画信息等...
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
排行榜、计数器
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

**发布/订阅**

**消息队列**

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

## 常用命令列表
```
字符串：可以存储字节串、整数、浮点数
Xxxx	命令	用例和描述
字符串常用命令	GET	Get key-name
	SET	Set key-name value
	DEL	Del key-name
字节串	INCR	INCR key-name:将键存储的值加1
	DECR	~ 将键存储的值加1
	INCRBY	INCRBY key-name amount: 将键存储的值加上整数amount
	DECRBY	DECRBY key-name amount: 将键存储的值减去整数amount
	INCRBYFLOAT	INCRBYFLOAT key-name amount: 将键存储的值加上浮点数amount，这个命名在Redis2.6及以上的版本可用
	APPEND	APPEND key-name value:将值value追加到给定键key-name当前存储的值的末尾
	GETRANGE /
SUBSTR	GETRANGE key-name start end:获取一个由偏移量start至end范围内所有字符组成的子串，包括start和end在内。
	SETRANGE	SETRANGE key-name start end:将从start偏移量开始的子串设置为给定值
	GETBIT	GETBIT key-name offset:将字节串看作是二进制位串，并返回位串中偏移量为offset的二进制位的值
	BITCOUNT	BITCOUNT key-name[start end]:统计二进制位串里面值为1的二进制的数量，如果给定了可选的start偏移量和end偏移量，那么只对偏移量指定范围内的二进制位进行统计。
	BITOP	BITOP operation dest-key key-name[key-name…]:对一个或多个二进制串执行包括并(and)、或(or)、异或(Xor)、非(not)在内的任意一种按位运算操作，并将计算得出的结果保存在dest-key键里面
整数		
浮点数		
列表
常用列表命令	RPUSH	RPUSH key-name value[value…]:将一个或多个值推入列表的右端
	LPUSH	同上
	RPOP	RPOP key-name:移除并返回列表最右端的元素
	LPOP	同上
	LINDEX	LINDEX key-name offset:返回列表中偏移量位offset的元素
	LRANGE	LRANGE key-name start end:返回列表从start偏移量到end偏移量范围内的所有元素，其中，偏移量为start和偏移量为end的元素也会包含在被返回的元素之内
	LTRIM	LTRIM key-name start end对列表进行修剪，只保留从start偏移量到end偏移量范围内的元素，其中偏移量为start和偏移量为end的元素也会被保留
阻塞式的列表弹出命令及在列表之间移动元素的命令	BLPOP	BLPOP key-name[key-name…]timeout:从第一个非空列表中弹出位于最左端的元素，或者在timeout秒之内阻塞并等待可弹出的元素出现
	BRPOP	同上
	RPOPLPUSH	RPOPLPUSH source-key dest-key:从source-key列表中弹出位于最右端的元素，然后将这个元素推入dest-key列表的最左端，并向用户返回这个元素
	BRPOPLPUSH	RPOPLPUSH source-key dest-key timeout:同上一样，如果source-key为空，那么在timeout秒之内阻塞并等待可弹出的元素出现
(对于阻塞Block弹出命令和弹出并推入命令，最常见的用例就是消息传递和任务队列)
集合
常用集合命令	SADD	SADD key-name item[item…]:将一个或多个元素添加到集合里面，并返回被添加元素当中原本并不存在于集合里面的元素数量
	SREM	SREM key-name item[item…]:从集合里面移除一个或多个元素，并返回被移除元素的数量
	SISMEMBER	SISMEMBER key-name item:检查元素item是否存在于集合key-name里
	SCARD	SCARD key-name:返回集合包含元素的数量
	SMEMBERS	SMEMBERS key-name:返回集合包含的所有元素
	SRANDMEMBER	SRANDMEMBER key-name[count]:从集合里面随机地返回一个或多个元素。Count为正时，命令返回的随机元素不会重复，count元素为负时，命令返回的随机元素可能会出现重复
	SPOP	SPOP key-name:随机地移除集合中的一个元素，并返回被移除的元素
	SMOVE	SMOVE source-key dest-key item:如果集合source-key包含元素item，那么从集合source-key里面移除元素item，并将元素item添加到dest-key中；如果item被成功移除，那么命令返回1，否则返回0
集合真正厉害的地方在于组合和关联多个集合
用于组合和处理多个集合的Redis命令	SDIFF	SDIFF key-name[key-name…]:返回那些存在于第一个集合，但不存在于其他集合的元素(数学中的差集运算)
	SDIFFSTORE	SDIFFSTORE dest-key key-name[key-name…]:将那些存在于第一个集合但不存在其他集合中的元素(数学中的差集)存储到dest-key键里面
	SINTER	SINTER key-name[key-name…]:返回那些同时存在于所有集合中的元素(数学中的交集)
	SINTERSTORE	SINTERSTORM dest-key key-name[key-name…]:将那些同时存在于所有集合中的元素存储到dest-key键里面
	SUNION	SUNION  key-name[key-name…]:返回那些至少存在一个集合中的元素(数学上的并集)
	SUNIONSTROE	SUNIONSTORE dest-key key-name[key-name…]:将那些至少存在于一个集合中的元素存储到dest-key键里面
散列
散列常用命令	HSET	在散列里面关联起给定的键值对
	HGET	获取指定散列键的值
	HGETALL	获取散列包含的所有键值对
	HDEL	如果给定键存在散列里面，那么移除这个键
用于添加和删除键值对的散列操作	HMGET	HMGET key-name key[key…]:从散列里面获取一个或多个键的值
	HMSET	HMSET key-name key value [key value…]:为散列里面的一个或多个键设置值
	HDEL	HDEL key-name key[key…]:删除散列里面的一个或多个键值对，返回成功找到并删除的键值对数量
	HLEN	HLEN key-name:返回散列包含的键值对数量
散列更高级的特性	HEXISTS	HEXISTS key-name key:检查给定的键是否存在于散列中
	HKEYS	HKEYS key-name:获取散列包含的所有键
	HVALS	HVALS key-name:获取散列包含的所有值
	HGETALL	HGETALL key-name:获取散列包含的所有键值对
	HINCRBY	HINCRBY key-name key increment:将键key存储的值加上整数increment
	HINCRBYFLOAT	HINCRBYFLOAT key-name key increment:将键key存储的值加上浮点数increment
有序集合
常用的有序集合命令	ZADD	ZADD key-name score member[score member…]:将带有给定分值的成员添加到有序集合中
	ZREM	ZREM key-name member[member]:从有序集合里面移除给定的成员，并返回被移除成员的数量
	ZCARD	ZCARD key-name:返回有序集合包含的成员数量
	ZINCRBY	ZINCRBY key-name increment member:将member成员的分值加上increment
	ZCOUNT	ZCOUNT key-name min max:返回分值介于min和max之间的成员数量
	ZRANK	ZRANK key-name member:返回成员member在有序集合中的排名
	ZSCORE	ZSCORE key-name member:返回成员member的分值
	ZRANGE	ZRANGE key-name start end[withscores]:返回有序集合中排名介于start和end之间的成员，如果给定了可选的withscores选项，那么命令会将成员的分值也一并返回
有序集合的范围型数据获取命令和范围型数据删除命令，以及并集和交集命令	ZREVRANK
	ZREVRANK key-name member:返回有序集合里成员member的排名，成员按照分值从大到小排列
	ZREVRANGE
	ZREVRANGE key-name start stop[withscores]:返回有序集合给定排名范围内的成员，成员按照分值从大到小排列
	ZRANGEBYSCORE
	ZRANGEBYSCORE key min max [withscores][limit offset count]返回有序集合中分值介于min和max之间的所有成员 
	ZREVRANGEBYSCORE
	ZREVRANGEBYSCORE key min max [withscores][limit offset count] 返回有序集合中分值介于min和max之间的所有成员，并按照分值从大到小的顺序来返回他们
	
ZREMRANGEBYRANK	ZREMRANGEBYRANK key-name start stop:移除有序集合中排名介于start和stop之间的所有成员
	ZREMRANGEBYSCORE
	ZREMRANGEBYSCORE key-name min max:移除有序集合中分值介于start和stop之间的所有成员
	ZINTERSTORE
	ZINTERSTORE dest-key key-count key [key…][weights weight[weight…]][aggregate sum|min|max]:对给定的有序集合执行类似于集合的交集运算
	ZUNIONSTORE	ZUNIONSTORE dest-key key-count key [key…][weights weight[weight…]][aggregate sum|min|max]:对给定的有序集合执行类似于集合的并集运算
发布与订阅
发布与订阅命令	SUBSCRIBE	SUBSCRIBE channel[channel…]:订阅给定的一个或多个频道
	UNSUBSCRIBE	UNSUBSCRIBE [channel[channel…]]:退订给定的一个或多个频道，如果执行时没有给定任何频道，那么退订所有频道
	PUBLISH	PUBLISH channel message:向给定频道发布消息
	PSUBSCRIBE	PSUBSCRIBE pattern[pattern…]:订阅与给定模式相匹配的所有频道
	PUNSUBSCRIBE	PUNSUBSCRIBE [pattern[pattern…]]:退订给定的模式，如果执行时没有给定任何模式，那么退订所有模式
其他命令
	SORT	SORT source-key [by pattern][limit offset count][get pattern [get pattern…]][ASC | DESC][ALPHA][STORE dest-key]:根据给定的选项，对输入列表、集合或者有序集合进行排序，然后返回或者存储排序的结果
可以把SORT命令看作是SQL语言中的order by子句。Sort是redis种唯一一个可以同时处理3种不同类型的数据的命令
事务命令
Redis事务命令	WATCH	
	MULTI	
	EXEC	
	UNWATCH	
	DISCARD	
处理过期时间的Redis命令
	PERSIST	PERSIST key-name:移除键的过期时间
	TTL	TTL key-name:查看给定键距离过期还有多少秒
	EXPIRE	EXPIRE key-name seconds:让给定键在指定的秒数之后过期
	EXPIREAT	EXPIREAT key-name timestamp:将给定键的过期时间设置为给定的UNIX时间戳
	PTTL	PTTL key-name:查看给定键距离过期时间还有多少毫秒，这个命令在redis2.6及以上可用
	PEXPIRE	PEXPIRE key-name milliseconds:让给定键在指定的毫秒数之后过期，redis2.6及以上可用
	PEXPIREAT	PEXPIREAT key-name timestamp-milliseconds:将一个毫秒级精度的UNIX时间戳设置为给定键的过期时间，Redis2.6及以上可用
		
		
		
		

```

# USE
```
https://myusf.usfca.edu/arts-sciences/computer-science
https://www.bilibili.com/video/av62657941?p=5
http://redisdoc.com/
https://redisson.org/
```

# 备注
```
分别使用redis的命令和redission去使用redis
```