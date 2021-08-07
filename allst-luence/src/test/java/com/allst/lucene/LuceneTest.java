package com.allst.lucene;

import com.allst.luence.demo.LuceneIndexDemo;
import org.junit.Test;

/**
 * @author June
 * @since 2021年08月
 */
public class LuceneTest {

    private final LuceneIndexDemo demo = new LuceneIndexDemo();

    @Test
    public void test() {
        demo.createIndex();
        System.out.println("create index file success ~");
    }

}
