select * from lagou;

--查看当前节点的所有数据库
show databases
--查看当前连接的数据库
db
--切换数据
use local
--删除数据库
db.dropDatabase()
db.help()
use admin
db.createUser({
user: "admin", pwd: "123456", roles: [
{role: "readWriteAnyDatabase", db: "admin"},
{role: "userAdminAnyDatabase", db: "admin"},
{role: "dbAdminAnyDatabase", db: "admin"}
]
})
db.createUser({
user: "hutu", pwd: "123456", roles: [
{role: "readWrite", db: "local"}
]
})
db.createUser({
user: "root", pwd: "123456", roles: [
{role: "root", db: "admin"}
]
})
show users
db
--创建集合
db.createCollection('users')
db.createCollection('items', {capped: true, size: 2048, max: 12})
-- 删除集合


