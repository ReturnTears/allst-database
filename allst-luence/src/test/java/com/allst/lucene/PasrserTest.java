package com.allst.lucene;

import com.allst.luence.demo.QueryDemo;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * @author June
 * @since 2021年08月
 */
public class PasrserTest {

    private final QueryDemo demo = new QueryDemo();

    /**
     * QueryParser搜索
     */
    @Test
    public void testPasr() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser("desc", analyzer);
        // 构建搜索对象
        Query query = queryParser.parse("desc:java AND name:lucene");
        demo.doSearch(query);
    }

    /**
     * MultiFieldQueryParser生成的语句为：name:lucene desc:lucene
     */
    @Test
    public void testSearchMultiFieldQueryParser() throws Exception {
        // 创建分词器
        Analyzer analyzer = new IKAnalyzer();
        // 1. 创建MultiFieldQueryParser搜索对象
        String[] fields = {"name", "desc"};
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(fields, analyzer);
        // 创建搜索对象
        Query query = multiFieldQueryParser.parse("lucene");
        // 打印生成的搜索语句
        System.out.println(query);
        // 执行搜索
        demo.doSearch(query);
    }

    /**
     * StandardQueryParser
     */
    @Test
    public void testStandardQueryParser() throws Exception {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 1. 创建Query搜索对象
        // 创建搜索解析器 传入分词器
        StandardQueryParser parser = new StandardQueryParser(analyzer);
        // 创建搜索对象
        Query query = parser.parse("desc:java AND name:lucene", "desc");
        // query = parser.parse("+desc:java + name:lucene", "desc");
        //通配符匹配 建议通配符在后 通配符在前效率低
        // query = parser.parse("name:L*", "desc");
        // query = parser.parse("name:L???", "desc");
        // 模糊匹配
        // query = parser.parse("lucene~", "desc");
        // 区间查询
        // query = parser.parse("id:[1 TO 100]", "desc");
        // 跨度查询 ~2表示词语之间包含两个词语
        // query = parser.parse("\"lucene java\"~2", "desc");
        // query = parser.parse("name:[Ha to jack]", "desc");
        // 打印生成的搜索语句
        System.out.println(query);
        // 执行搜索
        demo.doSearch(query);
    }
}
