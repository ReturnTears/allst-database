package com.allst.es.listener;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hutu
 * @since 2025-01-02 下午 10:37
 */
public class BinlogListener {
    public static void handle() {
        BinaryLogClient client = new BinaryLogClient("127.0.0.1", 3306, "root", "12345678");
        try {
            client.connect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        // 处理Binlog事件
        client.registerEventListener(BinlogListener::handleEvent);
        // 错误处理与重连机制
        client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                System.out.println("Connected to MySQL Binlog.");
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient binaryLogClient, Exception e) {

            }

            @Override
            public void onDisconnect(BinaryLogClient client) {
                System.out.println("Disconnected from MySQL Binlog. Trying to reconnect...");
                /*try {
                    client.reconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                System.out.println("Event deserialization failed: " + ex.getMessage());
            }
        });
    }

    private static void handleEvent(Event event) {
        // 根据事件类型进行处理
        System.out.println("Received event: " + event);
        EventType eventType = event.getHeader().getEventType();
        switch (eventType) {
            case WRITE_ROWS:
                WriteRowsEventData writeRowsEventData = event.getData();
                System.out.println("Insert data: " + writeRowsEventData.getRows());
                break;
            case UPDATE_ROWS:
                UpdateRowsEventData updateRowsEventData = event.getData();
                System.out.println("Update data: " + updateRowsEventData.getRows());
                break;
            case DELETE_ROWS:
                DeleteRowsEventData deleteRowsEventData = event.getData();
                System.out.println("Delete data: " + deleteRowsEventData.getRows());
                break;
            default:
                break;
        }
    }

    private static void handleEvent2(Event event) {
        EventType eventType = event.getHeader().getEventType();
        switch (eventType) {
            case WRITE_ROWS:
                WriteRowsEventData writeRowsEventData = event.getData();
                // 将插入数据同步到其他系统
                syncData(writeRowsEventData.getRows(), "INSERT");
                break;
            case UPDATE_ROWS:
                UpdateRowsEventData updateRowsEventData = event.getData();
                // 将更新数据同步到其他系统
                //syncData(updateRowsEventData.getRows(), "UPDATE");
                break;
            case DELETE_ROWS:
                DeleteRowsEventData deleteRowsEventData = event.getData();
                // 将删除数据同步到其他系统
                syncData(deleteRowsEventData.getRows(), "DELETE");
                break;
            default:
                break;
        }
    }

    private static void syncData(List<Serializable[]> rows, String operationType) {
        // 实现数据同步逻辑
        for (Serializable[] row : rows) {
            System.out.println("Syncing data: " + Arrays.toString(row) + " with operation " + operationType);
            // 这里可以调用其他系统的API或写入其他数据库
        }
    }
}
