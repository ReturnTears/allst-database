package com.allst.lucene;

import com.allst.luence.demo.*;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.junit.Test;

/**
 * @author June
 * @since 2021年08月
 */
public class QueryTest {

    private final QueryDemo demo = new QueryDemo();

    /**
     * TermQuery
     */
    @Test
    public void testTermQuery() {
        TermQuery termQuery = new TermQuery(new Term("name", "lucene"));
        try {
            demo.doSearch(termQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * BooleanQuery
     */
    @Test
    public void testBooleanQuery() throws Exception {
        // 创建两个 TermQuery搜索对象
        Query query1 = new TermQuery(new Term("name", "lucene"));
        Query query2 = new TermQuery(new Term("desc", "java"));
        // 创建BooleanQuery搜索对象,组合查询条件
        BooleanQuery.Builder boolQuery = new BooleanQuery.Builder();
        // 组合条件,
        // 第一个参数，查询条件，第二个参数，组合方式
        boolQuery.add(query1, BooleanClause.Occur.MUST);
        boolQuery.add(query2, BooleanClause.Occur.MUST);

        demo.doSearch(boolQuery.build());
    }

    /**
     * 短语查询PhraseQuery
     */
    @Test
    public void testPhraseQuery() throws Exception {
        // PhraseQuery query = new PhraseQuery("name","lucene");

        // slop: 表示跳过多少个值：Lucene Core is a Java library.... 这里表示lucene与java之间跳过三个值
        PhraseQuery query = new PhraseQuery(3, "desc", "lucene", "java");
        demo.doSearch(query);
    }

    /**
     * 效果同testPhraseQuery
     */
    @Test
    public void testSpanNearQuery() throws Exception {
        SpanTermQuery tq1 = new SpanTermQuery(new Term("desc", "lucene"));
        SpanTermQuery tq2 = new SpanTermQuery(new Term("desc", "java"));
        SpanNearQuery spanNearQuery = new SpanNearQuery(new SpanQuery[]{tq1, tq2}, 3, true);
        demo.doSearch(spanNearQuery);
    }

    @Test
    public void testLikeQuery() throws Exception {
        WildcardQuery wildcardQuery = new WildcardQuery(new Term("name", "so??"));
        demo.doSearch(wildcardQuery);

        // 效果同上，且有容错的能力
        // FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("name", "slorss"), 2);
        // demo.doSearch(fuzzyQuery);
    }

    /**
     * 数值查询
     */
    @Test
    public void testNumQuery() throws Exception {
        Query query = IntPoint.newRangeQuery("id", 1, 10);
        demo.doSearch(query);
    }
}
