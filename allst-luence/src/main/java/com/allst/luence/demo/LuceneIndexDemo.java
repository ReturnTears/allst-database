package com.allst.luence.demo;

import com.allst.luence.entity.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 索引的创建
 *
 * @author June
 * @since 2021年08月
 */
public class LuceneIndexDemo {
    /**
     * 索引创建
     */
    public void createIndex() {
        // 1、获取采集数据
        List<Book> books = DataCollect.getBooks();
        // 2、创建Document文档对象
        List<Document> documents = new ArrayList<>();
        for (Book book : books) {
            Document document = new Document();
            document.add(new TextField("id", book.getId().toString(), Field.Store.YES));
            document.add(new TextField("name", book.getName(), Field.Store.YES));
            document.add(new TextField("price", book.getPrice().toString(), Field.Store.YES));
            document.add(new TextField("desc", book.getDesc(), Field.Store.YES));
            document.add(new TextField("picture", book.getPicture(), Field.Store.YES));
            documents.add(document);
        }
        // 3、创建Analyzer 分词器， 对文档进行分词
        Analyzer analyzer = new StandardAnalyzer();
        // 创建Directory 和 IndexWriteConfig对象
        try {
            Directory directory = FSDirectory.open(Paths.get("E:/TestData/lucene/index"));
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            // 4、创建IndexWriter 写入对象
            IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
            // 添加文档对象
            for (Document doc : documents) {
                indexWriter.addDocument(doc);
            }
            // 释放资源
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
