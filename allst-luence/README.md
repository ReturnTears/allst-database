# Lucene
Lucene是一个开源的，高性能的，信息搜索库（IR：Information Retrieval Library）。

数据分类：
+ 结构化数据：
+ 非结构话数据（又叫全文数据）：

全文数据查询方法：
+ 1、顺序扫描法
```text
所谓顺序扫描，就是要找内容包含一个字符串的文件，就是一个文档一个文档的看，对于每一个文档，
从头看到尾，如果此文档包含此字符串，则此文档为我们要找的文件，接着看下一个文件，直到扫描完
所有的文件。
这种方法是数据量大就搜索慢。
```
+ 2、全文检索
```text
全文检索是指计算机索引程序通过扫描文章中的每一个词，对每一个词建立一个索引，指明该词在文章
中出现的次数和位置，当用户查询时，检索程序就根据事先建立的索引进行查找，并将查找的结果反馈
给用户的检索方式。这个过程类似于通过字典中的检索字表查字的过程。
```

Luence特性：``

使用luke工具查看Lucene索引
+ [查看索引1](src/main/resources/picture/luke_1.png)
+ [查看索引2](src/main/resources/picture/luke_2.png)
+ [查看索引3](src/main/resources/picture/luke_3.png)

## 注意实现
```text
使用索引搜索工具luke搜索我们创建的索引时， 需要版本匹配一致。
org.apache.lucene:8.9.0版本配合luke-swing-8.0.0-luke-release使用时会报错：
cannot open index path . not a valid lucene index directory or corrupted see
解决方法:
将lucene版本修改为：org.apache.lucene:7.7.3

```