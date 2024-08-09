# ElasticSearch

## API
```text
put https://127.0.0.1:9200/datadb/_doc/1
body {
    "count": 20
}
result {
    "_index": "datadb",
    "_id": "1",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 0,
    "_primary_term": 1
}

-- 查看索引库的映射信息
get https://127.0.0.1:9200/datadb/_mapping
result {
    "datadb": {
        "mappings": {
            "properties": {
                "count": {
                    "type": "long"
                }
            }
        }
    }
}

-- 创建索引库并禁用日期检测
put https://127.0.0.1:9200/myindex
body {
    "mappings": {
        "date_detection": false
    }
}
result {
    "acknowledged": true,
    "shards_acknowledged": true,
    "index": "myindex"
}

-- 删除索引库
delete https://127.0.0.1:9200/myindex
result {
    "acknowledged": true
}

-- 创建索引库
put https://127.0.0.1:9200/myindex

https://127.0.0.1:9200/myindex/_doc/1
body {
    "create_date": "2024/01/25"
}
result {
    "_index": "myindex",
    "_id": "1",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 0,
    "_primary_term": 1
}

get https://127.0.0.1:9200/myindex/_mapping
-- 禁用日期检测后， 没有把写入的”2024/01/25“映射成日期类型，而是text类型
result {
    "myindex": {
        "mappings": {
            "date_detection": false,
            "properties": {
                "create_date": {
                    "type": "text",
                    "fields": {
                        "keyword": {
                            "type": "keyword",
                            "ignore_above": 256
                        }
                    }
                }
            }
        }
    }
}


```