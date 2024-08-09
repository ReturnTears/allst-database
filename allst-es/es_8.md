# ElasticSearch 8.0
```text
ES版本下载：https://www.elastic.co/cn/downloads/past-releases/

以8.0为例：
下载zip包：elasticsearch-8.0.0-windows-x86_64.zip
解压到如下地址： D:\ProgramFiles\elasticsearch-8.0.0
运行命令：./bin/elasticsearch.bat
运行完成后会生成用户名和动态密码：
Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
  dxMukH8MKcRGHeH8DNrK

请求地址：https://127.0.0.1:9200/
输入对应的 username 和 password 即可看见如下信息：
{
    "name": "WIN-78EU4JQVMVG",
    "cluster_name": "elasticsearch",
    "cluster_uuid": "Avx5fOVdTw2HqsjzN3Bydg",
    "version": {
        "number": "8.0.0",
        "build_flavor": "default",
        "build_type": "zip",
        "build_hash": "1b6a7ece17463df5ff54a3e1302d825889aa1161",
        "build_date": "2022-02-03T16:47:57.507843096Z",
        "build_snapshot": false,
        "lucene_version": "9.0.0",
        "minimum_wire_compatibility_version": "7.17.0",
        "minimum_index_compatibility_version": "7.0.0"
    },
    "tagline": "You Know, for Search"
}

```
## api
```text
put https://127.0.0.1:9200/dbindex/_doc/1
{
  "name":"clay",
  "age":18,
  "actiontime":"2021-05-02 23:22:22.333",
  "id":123,
  // put已存在的文档_id,进行文档内容替换,不存在则新增
  "address": "China beijing"
}

get https://127.0.0.1:9200/dbindex/_doc/1
{
    "_index": "dbindex",
    "_id": "1",
    "_version": 2,
    "_seq_no": 1,
    "_primary_term": 1,
    "found": true,
    "_source": {
        "name": "clay",
        "age": 18,
        "actiontime": "2021-05-02 23:22:22.333",
        "id": 123,
        "address": "China beijing"
    }
}

get https://127.0.0.1:9200/_search
{
    "took": 926,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 5,
            "relation": "eq"
        },
        "max_score": 1.0,
        "hits": [
            {
                "_index": "dbindex",
                "_id": "1",
                "_score": 1.0,
                "_source": {
                    "name": "clay",
                    "age": 18,
                    "actiontime": "2021-05-02 23:22:22.333",
                    "id": 123,
                    "address": "China beijing"
                }
            },
            {
                "_index": "dbindex",
                "_id": "2",
                "_score": 1.0,
                "_source": {
                    "name": "kangkang",
                    "age": 29,
                    "address": "中国河南南阳"
                }
            },
            {
                "_index": "dbindex",
                "_id": "iuD4NJEBWbPx4H-kQumR",
                "_score": 1.0,
                "_source": {
                    "name": "suj",
                    "address": "china guiyang",
                    "work": "电力"
                }
            },
            {
                "_index": "dbindex",
                "_id": "3",
                "_score": 1.0,
                "_source": {
                    "name": "suj",
                    "address": "china guiyang",
                    "work": "电力"
                }
            },
            {
                "_index": "dbindex",
                "_id": "_search",
                "_score": 1.0,
                "_source": {
                    "name": "WSuJ",
                    "age": 29,
                    "address": "China Guiyang",
                    "work": "Electric Power"
                }
            }
        ]
    }
}

put https://127.0.0.1:9200/dbindex/_doc/2
{
  "name": "kangkang",
  "age": 29,
  "address": "中国河南南阳"
}

post https://127.0.0.1:9200/dbindex/_doc/3
{
    "name": "suj",
    "address": "china guiyang",
    "work": "电力",
    // post新增文档指定文档_id会报错
    "_id": 3
}

delete https://127.0.0.1:9200/dbindex/_doc/iuD4NJEBWbPx4H-kQumR
{
    "_index": "dbindex",
    "_id": "iuD4NJEBWbPx4H-kQumR",
    "_version": 2,
    "result": "deleted",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 6,
    "_primary_term": 1
}

-- 获取当前集群中所有的索引库信息
get https://127.0.0.1:9200/_cat/indices?v=true&pretty
health status index   uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   dbindex VOd1K5HpTnG56fCMP8oa3A   1   1          4            0       24kb           24kb

-- 获取当前集群中所有的别名索引信息
get https://127.0.0.1:9200/_cat/aliases?v=true&pretty
alias     index       filter routing.index routing.search is_write_index
.security .security-7 -      -             -              -

-- 获取当前集群中健康状态信息
get https://127.0.0.1:9200/_cat/health?v=true&pretty
epoch      timestamp cluster       status node.total node.data shards pri relo init unassign pending_tasks max_task_wait_time active_shards_percent
1723183742 06:09:02  elasticsearch yellow          1         1      3   3    0    0        1             0                  -                 75.0%

-- 获取当前集群中主节点信息
get https://127.0.0.1:9200/_cat/master?v=true&pretty
id                     host      ip        node
ix0DUGHXQueualwrNy0rqA 127.0.0.1 127.0.0.1 WIN-78EU4JQVMVG

-- 获取当前集群中索有节点信息
get https://127.0.0.1:9200/_cat/nodes?v=true&pretty
ip        heap.percent ram.percent cpu load_1m load_5m load_15m node.role   master name
127.0.0.1           13          48   3                          cdfhilmrstw *      WIN-78EU4JQVMVG

-- 获取当前集群中分片信息
get https://127.0.0.1:9200/_cat/shards?v=true&pretty
index            shard prirep state      docs store ip        node
dbindex          0     p      STARTED       4  24kb 127.0.0.1 WIN-78EU4JQVMVG
dbindex          0     r      UNASSIGNED                      
.geoip_databases 0     p      STARTED               127.0.0.1 WIN-78EU4JQVMVG
.security-7      0     p      STARTED               127.0.0.1 WIN-78EU4JQVMVG

-- 获取当前集群中健康状态信息
get https://127.0.0.1:9200/_cluster/health?pretty=true
{
    "cluster_name": "elasticsearch",
    "status": "yellow",
    "timed_out": false,
    "number_of_nodes": 1,
    "number_of_data_nodes": 1,
    "active_primary_shards": 3,
    "active_shards": 3,
    "relocating_shards": 0,
    "initializing_shards": 0,
    "unassigned_shards": 1,
    "delayed_unassigned_shards": 0,
    "number_of_pending_tasks": 0,
    "number_of_in_flight_fetch": 0,
    "task_max_waiting_in_queue_millis": 0,
    "active_shards_percent_as_number": 75.0
}

-- 获取当前集群中统计信息
get https://127.0.0.1:9200/_cluster/stats?pretty
{
    "_nodes": {
        "total": 1,
        "successful": 1,
        "failed": 0
    },
    "cluster_name": "elasticsearch",
    "cluster_uuid": "Avx5fOVdTw2HqsjzN3Bydg",
    "timestamp": 1723184834882,
    "status": "yellow",
    "indices": {
        "count": 3,
        "shards": {
            "total": 3,
            "primaries": 3,
            "replication": 0.0,
            "index": {
                "shards": {
                    "min": 1,
                    "max": 1,
                    "avg": 1.0
                },
                "primaries": {
                    "min": 1,
                    "max": 1,
                    "avg": 1.0
                },
                "replication": {
                    "min": 0.0,
                    "max": 0.0,
                    "avg": 0.0
                }
            }
        },
        "docs": {
            "count": 40,
            "deleted": 0
        },
        "store": {
            "size_in_bytes": 33538496,
            "total_data_set_size_in_bytes": 33538496,
            "reserved_in_bytes": 0
        },
        "fielddata": {
            "memory_size_in_bytes": 0,
            "evictions": 0
        },
        "query_cache": {
            "memory_size_in_bytes": 0,
            "total_count": 0,
            "hit_count": 0,
            "miss_count": 0,
            "cache_size": 0,
            "cache_count": 0,
            "evictions": 0
        },
        "completion": {
            "size_in_bytes": 0
        },
        "segments": {
            "count": 12,
            "memory_in_bytes": 0,
            "terms_memory_in_bytes": 0,
            "stored_fields_memory_in_bytes": 0,
            "term_vectors_memory_in_bytes": 0,
            "norms_memory_in_bytes": 0,
            "points_memory_in_bytes": 0,
            "doc_values_memory_in_bytes": 0,
            "index_writer_memory_in_bytes": 0,
            "version_map_memory_in_bytes": 0,
            "fixed_bit_set_memory_in_bytes": 0,
            "max_unsafe_auto_id_timestamp": -1,
            "file_sizes": {}
        },
        "mappings": {
            "field_types": [
                {
                    "name": "keyword",
                    "count": 4,
                    "index_count": 1,
                    "script_count": 0
                },
                {
                    "name": "long",
                    "count": 2,
                    "index_count": 1,
                    "script_count": 0
                },
                {
                    "name": "text",
                    "count": 4,
                    "index_count": 1,
                    "script_count": 0
                }
            ],
            "runtime_field_types": []
        },
        "analysis": {
            "char_filter_types": [],
            "tokenizer_types": [],
            "filter_types": [],
            "analyzer_types": [],
            "built_in_char_filters": [],
            "built_in_tokenizers": [],
            "built_in_filters": [],
            "built_in_analyzers": []
        },
        "versions": [
            {
                "version": "8.0.0",
                "index_count": 3,
                "primary_shard_count": 3,
                "total_primary_bytes": 33538496
            }
        ]
    },
    "nodes": {
        "count": {
            "total": 1,
            "coordinating_only": 0,
            "data": 1,
            "data_cold": 1,
            "data_content": 1,
            "data_frozen": 1,
            "data_hot": 1,
            "data_warm": 1,
            "ingest": 1,
            "master": 1,
            "ml": 1,
            "remote_cluster_client": 1,
            "transform": 1,
            "voting_only": 0
        },
        "versions": [
            "8.0.0"
        ],
        "os": {
            "available_processors": 20,
            "allocated_processors": 20,
            "names": [
                {
                    "name": "Windows 10",
                    "count": 1
                }
            ],
            "pretty_names": [
                {
                    "pretty_name": "Windows 10",
                    "count": 1
                }
            ],
            "architectures": [
                {
                    "arch": "amd64",
                    "count": 1
                }
            ],
            "mem": {
                "total_in_bytes": 34088599552,
                "adjusted_total_in_bytes": 34088599552,
                "free_in_bytes": 17662410752,
                "used_in_bytes": 16426188800,
                "free_percent": 52,
                "used_percent": 48
            }
        },
        "process": {
            "cpu": {
                "percent": 0
            },
            "open_file_descriptors": {
                "min": -1,
                "max": -1,
                "avg": 0
            }
        },
        "jvm": {
            "max_uptime_in_millis": 18193655,
            "versions": [
                {
                    "version": "17.0.1",
                    "vm_name": "OpenJDK 64-Bit Server VM",
                    "vm_version": "17.0.1+12",
                    "vm_vendor": "Eclipse Adoptium",
                    "bundled_jdk": true,
                    "using_bundled_jdk": true,
                    "count": 1
                }
            ],
            "mem": {
                "heap_used_in_bytes": 584784328,
                "heap_max_in_bytes": 2147483648
            },
            "threads": 121
        },
        "fs": {
            "total_in_bytes": 338595680256,
            "free_in_bytes": 300205355008,
            "available_in_bytes": 300205355008
        },
        "plugins": [],
        "network_types": {
            "transport_types": {
                "security4": 1
            },
            "http_types": {
                "security4": 1
            }
        },
        "discovery_types": {
            "zen": 1
        },
        "packaging_types": [
            {
                "flavor": "default",
                "type": "zip",
                "count": 1
            }
        ],
        "ingest": {
            "number_of_pipelines": 0,
            "processor_stats": {}
        }
    }
}

get https://127.0.0.1:9200/_nodes/stats?pretty=true
{
    ...
}

get https://127.0.0.1:9200/_stats?pretty=true
{
   ...
}


```

