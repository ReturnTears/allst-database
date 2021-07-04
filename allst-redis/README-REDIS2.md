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
```