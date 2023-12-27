package com.allst.trino.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 使用trino链接Hive
 *
 * @author Hutu
 * @since 2023-12-27 下午 09:24
 */
public class Trino2Hive {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:trino://172.20.10.5:8080/hive/sales";
        Properties properties = new Properties();
        properties.setProperty("user", "test");
        properties.setProperty("password", "secret");
        properties.setProperty("SSL", "true");
        Connection connection = DriverManager.getConnection(url, properties);
        String schema = connection.getSchema();
        System.out.println("schema : " + schema);
    }
}
