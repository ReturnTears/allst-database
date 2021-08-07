package com.allst.luence.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

import static com.allst.luence.uitl.CommonField.LUCENE_INDEX_PATH;

/**
 * 索引的搜索
 * score:1.4318397
 * score:0.45101056
 * 分数系数为：score1 / score2 （即分数高 / 分数低）
 *
 * @author June
 * @since 2021年08月
 */
public class SearchIndexDemo {
    public void searchIndex() throws Exception {
        // 1、创建Query 搜索对象
        // 1.1、创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 1.2、创建搜索解析器
        QueryParser queryParser = new QueryParser("id", analyzer);
        // 1，3、创建搜索对象
        Query query = queryParser.parse("desc:java OR name:solr");
        // 2、创建Directory 流对象  指定索引库位置
        Directory directory = FSDirectory.open(Paths.get(LUCENE_INDEX_PATH));
        // 3、创建索引读取对象
        IndexReader indexReader = DirectoryReader.open(directory);
        // 4、创建索引搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        // 5、执行搜索 返回结果集 TopDocs
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("search result nums : " + topDocs.totalHits);
        // 获取排序的文档
        ScoreDoc[] docs = topDocs.scoreDocs;
        // 6、解析结果集
        for (ScoreDoc scoreDoc : docs) {
            // 获取文档id
            int docID = scoreDoc.doc;
            Document doc = indexSearcher.doc(docID);
            System.out.println("score:" + scoreDoc.score);

            System.out.println("docId:" + docID);
            System.out.println("id:" + doc.get("id"));
            System.out.println("name:" + doc.get("name"));
            System.out.println("price:" + doc.get("price"));
            System.out.println("desc:" + doc.get("desc"));
            System.out.println("picture:" + doc.get("picture"));
        }
        indexReader.close();
    }
}
