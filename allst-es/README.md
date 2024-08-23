# Elasticsearch
```text
查询所有的文档：
https://127.0.0.1:9200/_search

查询索引 books 下的文档：
https://127.0.0.1:9200/books/_search

查询所有的索引：
https://127.0.0.1:9200/_cat/indices?v

查询索引 video 下的文档：
https://127.0.0.1:9200/video/_search

原始DSL查询:
http://127.0.0.1:9200/video/_search
请求体：{
  "query": {
    "bool": {
      "must": [{
        "match": {
          "title": "架构"
        }
      }, {
        "match": {
          "description": "spring"
        }
      }, {
        "range": {
          "duration": {
            "gte": 10,
            "lte": 6000
          }
        }
      }]
    }
  }
}

聚合查询：
1、统计不同分类下的视频数量：
http://127.0.0.1:9200/video/_search
请求体：
{
  "size": 1,
  "aggs": {
    "category_group": {
      "terms": {
        "field": "category"
      }
    }
  }
}

分词查询
http://127.0.0.1:9200/video/_analyze
请求体：
{
  "field": "title",
  "text": "我今天去小滴课堂学习spring cloud项目大课"
}

```

## ES搜索引擎
```text
教程1
https://www.cnblogs.com/buchizicai/p/17093719.html

访问地址： https://www.cnblogs.com/xietingwei/p/18027313#springboot3x%E6%95%B4%E5%90%88es

```