package com.allst.db.mongo;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

/**
 * @author Hutu
 * @since 2024-02-29 下午 11:50
 */
public class MongoAuthDemo {
    public static void main(String[] args) {
        findMongo();
    }

    private static void insertMongo() {
        MongoClient mongoClient = MongoClients.create("mongodb://admin:123456@localhost:27017/admin");
        // 获取数据库对象
        MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");
        // 获取集合对象
        MongoCollection<Document> collection = mongoDatabase.getCollection("lagou");
        // 构建Document对象，并插入到集合中
        Document document = Document.parse("{'name' : 'kangkang', 'birthday' : ISODate('2002-01-07T11:11:11Z'), 'salary' : 15000, 'city' : 'beijing' }");
        collection.insertOne(document);
        // 关闭客户端
        mongoClient.close();
    }

    private static void findMongo() {
        MongoClient mongoClient = MongoClients.create("mongodb://admin:123456@localhost:27017/admin");
        // 获取数据库对象
        MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");
        // 获取集合对象
        MongoCollection<Document> collection = mongoDatabase.getCollection("lagou");
        // 指定字段排序
        Document doc = new Document();
        doc.append("salary", -1); // 按照salary倒序
        FindIterable<Document> documents = collection.find(Filters.gt("salary", 20000)).sort(doc); // 按照salary过滤
        // old过滤版本
        // FindIterable<Document> docs = collection.find(Document.parse("{salary:${gt:20000}}")).sort(doc); // 按照salary过滤
        for (Document document : documents) {
            System.out.println(document);
        }
        // 关闭客户端
        mongoClient.close();
    }
}
