package com.allst.luence.demo;

import com.allst.luence.entity.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.allst.luence.uitl.CommonField.LUCENE_INDEX_PATH_2;

/**
 * @author June
 * @since 2021年08月
 */
public class AnalyzerDemo {

    public void createIndex() throws Exception {
        // 1. 采集数据  和 之前完全相同
        List<Book> bookList = new ArrayList<>();
        Book booka = new Book();
        booka.setId(1);
        // booka.setDesc("Lucene is java full text search lib");
        booka.setDesc("我是中国人，我爱中国");
        booka.setName("lucene");
        booka.setPrice(100.45f);
        bookList.add(booka);
        // 2. 创建Document文档对象
        List<Document> documents = new ArrayList<>();
        for (Book book : bookList) {
            Document document = new Document();
            // IntPoint  分词 索引 不存储 存储结合  StoredField
            Field id = new IntPoint("id", book.getId());
            Field id_v = new StoredField("id", book.getId());
            // 分词、索引、存储 TextField
            Field name = new TextField("name", book.getName(), Field.Store.YES);
            // 分词、索引、不存储 但是是数字类型，所以使用FloatPoint
            Field price = new FloatPoint("price", book.getPrice());
            // 分词、索引、存储 TextField 为了看到分词效果设置成存储
            Field desc = new TextField("desc", book.getDesc(), Field.Store.YES);
            // 将field域设置到Document对象中
            document.add(id);
            document.add(id_v);
            document.add(name);
            document.add(price);
            document.add(desc);
            documents.add(document);
        }
        //3.创建StandardAnalyzer 分词器 对文档进行分词  把 StandardAnalyzer 变成 IKAnalyer
        //Analyzer analyzer  = new StandardAnalyzer();
        // ik分词器
        Analyzer analyzer = new IKAnalyzer();
        // 创建Directory   和 IndexWriterConfig 对象
        Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH_2));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        // 4.创建IndexWriter 写入对象
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        // 添加文档对象
        for (Document doc : documents) {
            indexWriter.addDocument(doc);
        }
        // 释放资源
        indexWriter.close();
    }

}
