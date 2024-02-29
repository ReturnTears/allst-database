package com.allst.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

/**
 * @author June
 * @since 2021年10月
 */
public class MongoDemo {
    public static void main(String[] args) {
        // 插入
        insertMongo();
        // 查询
        // findMongo();
    }

    private static void insertMongo() {
        // 无密码时可以使用此方式
        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        // 获取数据库对象
        MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");
        // 获取集合对象
        MongoCollection<Document> collection = mongoDatabase.getCollection("lagou");
        // 构建Document对象，并插入到集合中
        Document document = Document.parse("{'name' : 'xiaohuzi', 'birthday' : ISODate('2000-03-07T00:00:00Z'), 'salary' : 45000, 'city' : 'guangzhou' }");
        collection.insertOne(document);
        // 关闭客户端
        mongoClient.close();
    }

    /**
     * 结果为：
     * Document{{_id=6160492be6cb68583856e7d8, name=kangkang, birthday=Thu Oct 01 08:00:00 CST 2020, salary=15000.0, city=beijing}}
     * Document{{_id=61604e97245f7a8dc8ee908b, name=xiaohu, birthday=Tue Jun 11 08:00:00 CST 2019, salary=25000.0, city=chongqing}}
     * Document{{_id=61604e97245f7a8dc8ee908c, name=kangshuai, birthday=Sun Nov 11 08:00:00 CST 2018, salary=15000.0, city=xian}}
     * Document{{_id=61644b9cd5324e69b8a726be, name=xiaohuzi, birthday=Tue Mar 07 08:00:00 CST 2000, salary=45000, city=guangzhou}}
     * 倒序：
     * Document{{_id=61644b9cd5324e69b8a726be, name=xiaohuzi, birthday=Tue Mar 07 08:00:00 CST 2000, salary=45000, city=guangzhou}}
     * Document{{_id=61604e97245f7a8dc8ee908b, name=xiaohu, birthday=Tue Jun 11 08:00:00 CST 2019, salary=25000.0, city=chongqing}}
     * Document{{_id=6160492be6cb68583856e7d8, name=kangkang, birthday=Thu Oct 01 08:00:00 CST 2020, salary=15000.0, city=beijing}}
     * Document{{_id=61604e97245f7a8dc8ee908c, name=kangshuai, birthday=Sun Nov 11 08:00:00 CST 2018, salary=15000.0, city=xian}}
     * 过滤：
     * Document{{_id=61644b9cd5324e69b8a726be, name=xiaohuzi, birthday=Tue Mar 07 08:00:00 CST 2000, salary=45000, city=guangzhou}}
     * Document{{_id=61604e97245f7a8dc8ee908b, name=xiaohu, birthday=Tue Jun 11 08:00:00 CST 2019, salary=25000.0, city=chongqing}}
     */
    private static void findMongo() {
        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
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
