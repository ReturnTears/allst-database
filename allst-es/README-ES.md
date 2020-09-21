# ES
## ElasticSearch7.6.x+
```text
ElasticSearch是基于Lucene做了一些封装和增强

```

## Windows下安装ElasticSearch
```text
在华为镜像服务器上下载相应的文件包:https://mirrors.huaweicloud.com/
elasticsearch、kibana、logstash
Windows下elasticsearch是开箱即用的，在config目录下，elasticsearch.yml中添加跨域配置:
http.cors.enabled: true
http.cors.allow-origin: 
启动elasticsearch:
点击bin目录下elasticsearch.bat脚本
启动完成后: http://127.0.0.1:9200

下载可视化开源工具:https://github.com/mobz/elasticsearch-head
在elasticsearch-head-master目录中安装相关依赖包：
使用淘宝安装: cnpm install
启动elasticsearch-head: npm run start
启动完成后:http://127.0.0.1:9100
```