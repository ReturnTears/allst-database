package com.allst.db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AllstMongodbApplicationTests {

    /**
     * 以配置文件方式启动端口为：27017， 默认为：37017
     */
    @Test
    void contextLoads() {
        MongoClient mongoClient = new MongoClient("192.168.0.107", 27017);
        // 获取数据库对象
        MongoDatabase mongoDatabase = mongoClient.getDatabase("test");
        // 获取集合对象
        MongoCollection<Document> collection = mongoDatabase.getCollection("lagou");
        // 构建Document对象，并插入到集合中
        Document document = Document.parse("{'name' : 'xiaohuzi', 'birthday' : ISODate('2000-03-07T00:00:00Z'), 'salary' : 45000, 'city' : 'guangzhou' }");
        collection.insertOne(document);
        // 关闭客户端
        mongoClient.close();
    }

}
