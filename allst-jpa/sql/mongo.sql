show databases
use admin
db.createCollection('person')
db.person.insert({id:1, name:"张三", age: 18, address:"北京"})
db.person.insertOne({id:2, name:"lisi", age: 20, address:"上海"})
db.person.find()