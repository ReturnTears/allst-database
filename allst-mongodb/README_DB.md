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
db.users.drop()
-- 查看集合
show collections
-- 插入文档
db.createCollection('users')
db.users.insert({name:"fkjava", age: 29})
db.users.insertOne({address:"guangzhou", zip: 510000})
db.collection.find()
db.users.find()
db.users.insertOne({_id: 101, gender:"male",email: "sun@fkjava.org"})
db.products.insert([
{_id:11,item:"pencil",qty:50,type:"n0.2"},
{item:"pen",qty:20},
{item:"eraser",qty:25}
])
db.products.find()
db.products.insertMany([
{_id:20,item:"lamp",qty:50,type:"desk"},
{_id:21,item:"lamp",qty:20,type:"floor"},
{_id:22,item:"bulk",qty:100}
],{ordered: false})
db.products.insertOne(
{item:"envelops",qty:100,type:"Clasp"},
{writeConcern: {w: "majority", wtimeout:5000}}
)
db.users.find()
db.users.update(
{name: "fkjava"},
{$inc: {age: 18},$set:{name:"疯狂Java",address: "广州"}}
)
-- 上述语句相当于:update users set age = age + 18, name = "疯狂Java", address = "广州" where name = "fkjava"
-- 下面第一个语句报错，修改为第二个语句
db.users.update(
{address: {$exists: false}},
{$set: {favotite: "Java", gender: "男"}},
{multi: true}
)
db.users.updateMany(
{address: {$exists: false}},
{$set: {favotite: "Java", gender: "男"}}
)

