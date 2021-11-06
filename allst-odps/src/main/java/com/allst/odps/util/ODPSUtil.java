package com.allst.odps.util;

import com.aliyun.odps.*;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.task.SQLTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ODPS工具类
 *
 * @author June
 * @since 2021年11月
 */
public class ODPSUtil {

    /**
     * odps连接
     *
     * @param endPoint  地址
     * @param accessId  用户名
     * @param accessKey 密钥
     * @param project   数据库名称
     *
     * @return 结果
     */
    public static Odps getConn(String endPoint, String accessId, String accessKey, String project) {
        Account account = new AliyunAccount(accessId, accessKey);
        Odps odps = new Odps(account);
        odps.setEndpoint(endPoint);
        odps.setDefaultProject(project);
        return odps;
    }

    /**
     * 获取OPDS的tables
     *
     * @param stmt opds已连接对象
     *
     * @return 结果
     */
    public ArrayList<Map<String, String>> showTables(Odps stmt) {
        ArrayList<Map<String, String>> tableList = new ArrayList<>();
        Tables tables = stmt.tables();
        for (Table table : tables) {
            Map<String, String> tableInfo = new HashMap<>();
            tableInfo.put("tableName", table.getName());
            tableInfo.put("remarks", table.getComment());
            tableList.add(tableInfo);
        }
        return tableList;
    }


    /**
     * 获取OPDS的字段colunmn的信息
     *
     * @param stmt      opds 已连接对象
     * @param tableName 表名
     *
     * @return 结果
     *
     * @throws OdpsException 异常
     */
    public ArrayList<Map<String, String>> showTableFields(Odps stmt, String tableName) throws OdpsException {
        ArrayList<Map<String, String>> tableFieldList = new ArrayList<>();
        Table t = stmt.tables().get(tableName);
        t.reload();
        TableSchema schema = t.getSchema();
        List<Column> columns = schema.getColumns();
        for (Column column : columns) {
            Map<String, String> fieldInfo = new HashMap<>();
            fieldInfo.put("columnName", column.getName());
            fieldInfo.put("remarks", column.getComment());
            tableFieldList.add(fieldInfo);
        }
        return tableFieldList;
    }

    /**
     * 获取表的数据预览
     *
     * @param stmt      odps连接
     * @param tableName 表名
     *
     * @throws OdpsException 异常
     */
    public void getTableData(Odps stmt, String tableName) throws OdpsException {
        String sql = String.format("select * from %S limit 5;", tableName);
        Instance i = SQLTask.run(stmt, sql);
        i.waitForSuccess();
        List<Record> records = SQLTask.getResult(i);
        for (Record r : records) {
            String field1 = r.getString(1);
            String field2 = r.getString("name");
        }
    }

    /**
     * 创建普通表
     *
     * @param stmt      odps连接
     * @param tableName 表名
     */
    public static boolean createTable(Odps stmt, String tableName) {
        String createSQL = String.format("create table if not exists %s ( %s );", tableName,
                "id bigint comment '主键id', " +
                        " name string comment '姓名'," +
                        " age bigint comment '年龄'," +
                        " sex string comment '性别'  ");
        try {
            Instance run = SQLTask.run(stmt, createSQL);
            run.waitForSuccess();
            return run.isSuccessful();
        } catch (OdpsException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建分区表
     *
     * @param stmt      odps连接
     * @param tableName 表名
     * @param partition 分区名称
     *
     * @return 结果
     */
    public static boolean createPartTable(Odps stmt, String tableName, String partition) {
        return false;
    }

    /**
     * 删除表， 暂不支持delete删除表数据
     *
     * @param stmt      ODPS连接
     * @param tableName 表名
     *
     * @return 结果
     */
    public static boolean dropTable(Odps stmt, String tableName) {
        String insertSQL = String.format("drop table %s if exists;", tableName);
        try {
            Instance run = SQLTask.run(stmt, insertSQL);
            run.waitForSuccess();
            return run.isSuccessful();
        } catch (OdpsException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 新增普通表数据
     *
     * @param stmt      odps连接
     * @param tableName 表名
     *
     * @return 结果
     */
    public static boolean insertTable(Odps stmt, String tableName) {
        String insertSQL = String.format("insert into %s xxx ;", tableName);
        return execSQL(stmt, insertSQL);
    }

    /**
     * 新增分区表数据
     *
     * @param stmt      odps连接
     * @param tableName 表名
     * @param partition 分区名称
     *
     * @return 结果
     */
    public static boolean insertPartTable(Odps stmt, String tableName, String partition) {

        return false;
    }

    /**
     * 更新普通表数据
     *
     * @param stmt      odps连接
     * @param tableName 表名
     *
     * @return 结果
     */
    public static boolean updateTable(Odps stmt, String tableName) {
        String updateSQL = String.format("insert overwrite %s", tableName);
        return execSQL(stmt, updateSQL);
    }

    /**
     * 更新普通表数据
     *
     * @param stmt      odps连接
     * @param tableName 表名
     * @param partition 分区名称
     *
     * @return 结果
     */
    public static boolean updatePartTable(Odps stmt, String tableName, String partition) {
        String updateSQL = String.format("insert overwrite %s xxx %s", tableName, partition);
        return execSQL(stmt, updateSQL);
    }

    /**
     * 执行SQL语句
     *
     * @param stmt odps连接
     * @param sql  表名
     *
     * @return 结果 true |　false
     */
    private static boolean execSQL(Odps stmt, String sql) {
        try {
            Instance run = SQLTask.run(stmt, sql);
            run.waitForSuccess();
            return run.isSuccessful();
        } catch (OdpsException e) {
            e.printStackTrace();
        }
        return false;
    }
}
