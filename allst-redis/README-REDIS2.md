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
```