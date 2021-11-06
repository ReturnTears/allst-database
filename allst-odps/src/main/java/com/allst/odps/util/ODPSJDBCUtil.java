package com.allst.odps.util;

import java.sql.*;
import java.util.Properties;

/**
 * @author June
 * @since 2021年11月
 */
public class ODPSJDBCUtil {

    private static final String driverName = "com.aliyun.odps.jdbc.OdpsDriver";

    public static void main(String[] args) throws SQLException {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Properties config = new Properties();
        config.put("access_id", "LTAI*********SPbd");
        config.put("access_key", "AC5K*******************GdnVk");
        config.put("project_name", "market_***_analyze");
        Connection conn = DriverManager.getConnection("jdbc:odps:http://service.cn.maxcompute.aliyun.com/api", config);
        ResultSet rs;

        // 建表
        Statement stmt = conn.createStatement();
        String tableName = "market_repay_report";
//        stmt.execute("drop table if exists " + tableName);
//        stmt.execute("create table " + tableName + " (key int, value string)");

// metadata
//        DatabaseMetaData metaData = conn.getMetaData();
//        System.out.println("product = " + metaData.getDatabaseProductName());
//        System.out.println("jdbc version = " + metaData.getDriverMajorVersion() + ", "
//                + metaData.getDriverMinorVersion());
//        rs = metaData.getTables(null, null, tableName, null);
//        while (rs.next()) {
//            String name = rs.getString(3);
//            System.out.println("inspecting table: " + name);
//            ResultSet rs2 = metaData.getColumns(null, null, name, null);
//            while (rs2.next()) {
//                System.out.println(rs2.getString("COLUMN_NAME") + "\t"
//                        + rs2.getString("TYPE_NAME") + "(" + rs2.getInt("DATA_TYPE") + ")");
//            }
//        }

        String sql;

// 插
//        sql = String.format("insert into table %s select 24 key, ‘hours’ value from (select count(1) from %s) a", tableName, tableName);
//        System.out.println("Running: " + sql);
//        int count = stmt.executeUpdate(sql);
//        System.out.println("updated records: " + count);

//查
        sql = "select * from " + tableName + " where pt=to_char(dateadd(from_unixtime(unix_timestamp()), -1, 'dd'), 'yyyymmdd')";
        System.out.println("Running: " + sql);
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(String.valueOf(rs.getInt(1)) + "\t" + rs.getString(2));
        }

        if (rs != null) rs.close();
        if (stmt != null) stmt.close();
        if (conn != null) conn.close();

    }

}
