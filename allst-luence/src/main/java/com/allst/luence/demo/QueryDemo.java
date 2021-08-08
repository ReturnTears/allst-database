package com.allst.luence.demo;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

import static com.allst.luence.uitl.CommonField.LUCENE_INDEX_PATH;

/**
 * @author June
 * @since 2021年08月
 */
public class QueryDemo {
    public void doSearch(Query query) throws Exception {
        // 创建Directory 流对象
        Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH));
        // 创建IndexReader
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        // 获取TopDocs
        TopDocs topDocs = searcher.search(query, 10);
        System.out.println("查询索引总条数:" + topDocs.totalHits);
        ScoreDoc[] docs = topDocs.scoreDocs;
        // 解析结果集
        for (ScoreDoc scoreDoc : docs) {
            int docID = scoreDoc.doc;
            Document document = searcher.doc(docID);
            System.out.println("docID : " + docID);
            System.out.println("bookId : " + document.get("id"));
            System.out.println("name : " + document.get("name"));
            System.out.println("price : " + document.get("price"));
            System.out.println("desc : " + document.get("desc"));
        }
        indexReader.close();
    }
}
