package com.allst.es.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.allst.es.config.MySqlConfig;
import com.allst.es.entity.EsUser;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:38
 */
@Component
public class MySQLBinlogListener {
    @Autowired
    private MySqlConfig mySQLConfig;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    public void execListenerMysql() {
        BinaryLogClient client = new BinaryLogClient(mySQLConfig.getHostname(), mySQLConfig.getPost(), mySQLConfig.getUsername(), mySQLConfig.getPassword());
        client.setServerId(2);

        client.registerEventListener(event -> {
            EventData data = event.getData();
            String db = null;
            if (data instanceof TableMapEventData) {
                TableMapEventData tableMapEventData = (TableMapEventData) data;
                System.out.println("Table:");
                db = tableMapEventData.getDatabase();
                System.out.println(tableMapEventData.getTableId() + ": [" + db + "-" + tableMapEventData.getTable() + "]");
            }
            if (data instanceof UpdateRowsEventData) {
                System.out.println("Update:");
                System.out.println(data);
            } else if (data instanceof WriteRowsEventData) {
                System.out.println("Insert:");
                System.out.println(data);
                //if (StrUtil.equals("demo", db)) {
                createIndex();
                String jsonStr = JSON.toJSONString(data);
                JSONObject jsonObj = JSON.parseObject(jsonStr);
                JSONArray rows = (JSONArray) jsonObj.get("rows");
                for (Object row : rows) {
                    JSONArray array = JSON.parseArray(row.toString());
                    Object o = array.get(0);
                    Long id = Long.parseLong(o.toString());
                    EsUser esUser = new EsUser(id, (String) array.get(1), (Integer) array.get(2), (String) array.get(3));
                    elasticsearchTemplate.save(esUser);
                }
                //}
            } else if (data instanceof DeleteRowsEventData) {
                System.out.println("Delete:");
                System.out.println(data);
            }
        });
        try {
            client.connect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createIndex() {
        // spring data es所有索引操作都在这个接口
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(EsUser.class);
        // 是否存在，存在则删除
        if (indexOperations.exists()) {
            //indexOperations.delete();
            return;
        }
        // 创建索引
        indexOperations.create();
        // 设置映射: 在正式开发中，几乎不会使用框架创建索引或设置映射，这是架构或者管理员的工作，不适合使用代码实现
        elasticsearchTemplate.indexOps(EsUser.class).putMapping();
    }
}
