package com.allst.lucene;

import com.allst.luence.demo.LuceneFieldDemo;
import com.allst.luence.demo.LuceneIndexDemo;
import com.allst.luence.demo.SearchIndexDemo;
import org.junit.Test;

/**
 * @author June
 * @since 2021年08月
 */
public class LuceneTest {

    private final LuceneIndexDemo demo = new LuceneIndexDemo();

    private final SearchIndexDemo searchIndexDemo = new SearchIndexDemo();

    private final LuceneFieldDemo fieldDemo = new LuceneFieldDemo();

    @Test
    public void testCreateIndex() {
        demo.createIndex();
        System.out.println("create index file success ~");
    }

    @Test
    public void testSearchIndex() {
        try {
            searchIndexDemo.searchIndex();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("search index file success ~");
        }
    }

    @Test
    public void testFieldIndex() {
        fieldDemo.fieldIndex();
    }

    /**
     * 在现有索引的基础上添加索引
     */
    @Test
    public void testIndexCreate() {
        try {
            fieldDemo.indexCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在现有索引的基础上删除索引
     */
    @Test
    public void testIndexDelete() {
        try {
            fieldDemo.indexDelete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在现有索引的基础上全部删除
     */
    @Test
    public void testIndexDeleteAll() {
        try {
            fieldDemo.indexDeleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新索引
     */
    @Test
    public void testIndexUpdate() {
        try {
            fieldDemo.indexUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
