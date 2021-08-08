package com.allst.luence.demo;

import com.allst.luence.entity.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.allst.luence.uitl.CommonField.LUCENE_INDEX_PATH;

/**
 * @author June
 * @since 2021年08月
 */
public class LuceneFieldDemo {

    public void fieldIndex() {
        // 1、获取采集数据
        List<Book> books = DataCollect.getBooks();
        // 2、将采集到的数据封装在Document对象中
        List<Document> documents = new ArrayList<>();
        Document document;
        for (Book book : books) {
            Field id = new IntPoint("id", book.getId());
            Field name = new TextField("name", book.getName(), Field.Store.YES);
            Field price = new FloatPoint("price", book.getPrice());
            Field desc = new TextField("desc", book.getDesc(), Field.Store.NO);
            document = new Document();
            document.add(id);
            document.add(name);
            document.add(price);
            document.add(desc);
            documents.add(document);
        }
        // 3、创建 Analyzer 分词器， 对文档进行分词
        Analyzer analyzer = new StandardAnalyzer();
        // 4、创建 Directory 和 IndexWriterConfig 对象
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH));
            // 5、创建 IndexWriter 写入对象
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
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

    /**
     * 添加索引
     */
    public void indexCreate() throws Exception {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 创建Directory流对象
        Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 创建写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        // 创建Document
        Document document = new Document();
        document.add(new TextField("id", "1001", Field.Store.YES));
        document.add(new TextField("name", "game", Field.Store.YES));
        document.add(new TextField("desc", "one world one dream", Field.Store.NO));
        // 添加文档 完成索引添加
        indexWriter.addDocument(document);
        // 释放资源
        indexWriter.close();
    }

    /**
     * 索引删除
     */
    public void indexDelete() throws IOException {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 创建Directory流对象
        Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 创建写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        // 添加文档 完成索引删除
        indexWriter.deleteDocuments(new Term("name", "game"));
        // 释放资源
        indexWriter.close();
    }

    /**
     * 全部删除
     */
    public void indexDeleteAll() throws IOException {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 创建Directory流对象
        Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 创建写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        // 添加文档 完成索引删除
        indexWriter.deleteAll();
        // 释放资源
        indexWriter.close();
    }

    /**
     * 更新索引
     * 更新索引是先删除再添加，建议对更新需求采用此方法并且要保证对已存在的索引执行更新，可以先查
     * 询出来，确定更新记录存在执行更新操作。
     * 如果更新索引的目标文档对象不存在，则执行添加。
     */
    public void indexUpdate() throws Exception {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 创建Directory流对象
        Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH));
        // 创建写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        // 创建Document
        Document document = new Document();
        document.add(new TextField("id", "1002", Field.Store.YES));
        document.add(new TextField("name", "lucene test update1002", Field.Store.YES));
        // 执行更新，会把所有符合条件的Document删除，再新增。
        indexWriter.updateDocument(new Term("name", "test"), document);
        // 释放资源
        indexWriter.close();
    }
}
