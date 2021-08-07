package com.allst.luence.demo;

import com.allst.luence.entity.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据采集
 *
 * @author June
 * @since 2021年08月
 */
public class DataCollect {
    public static List<Book> getBooks() {
        List<Book> bookList = new ArrayList<>();
        Book booka = new Book();
        booka.setId(1);
        booka.setDesc("Lucene Core is a Java library providing powerful indexing and search features, as well as spellchecking, hit highlighting and advanced analysis/tokenization capabilities. The PyLucene sub project provides Python bindings for Lucene Core. ");
        booka.setName("Lucene");
        booka.setPrice(100.45f);
        booka.setPicture("booka.png");
        bookList.add(booka);

        Book bookb = new Book();
        bookb.setId(11);
        bookb.setDesc("Solr is highly scalable, providing fully fault tolerant distributed indexing, search and analytics. It exposes Lucene's features through easy to use JSON/HTTP interfaces or native clients for Java and other languages. ");
        bookb.setName("Solr");
        bookb.setPrice(320.45f);
        bookb.setPicture("bookb.jpg");
        bookList.add(bookb);

        Book bookc = new Book();
        bookc.setId(21);
        bookc.setDesc("The Apache Hadoop software library is a framework that allows for the distributed processing of large data sets across clusters of computers using simple programming models.");
        bookc.setName("Hadoop");
        bookc.setPrice(620.45f);
        bookc.setPicture("bookc.jif");
        bookList.add(bookc);
        return bookList;
    }
}
