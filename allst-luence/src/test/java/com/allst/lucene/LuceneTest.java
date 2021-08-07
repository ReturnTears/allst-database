package com.allst.lucene;

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
}
