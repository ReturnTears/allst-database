package com.allst.boot.ops;

import com.allst.boot.influx.sql.InfluxQl;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public final class PortableBackupCommands {

    private static final Pattern HOST = Pattern.compile("^[A-Za-z0-9_.:-]{1,128}$");
    private static final Pattern COMMAND = Pattern.compile("^[A-Za-z0-9_.:\\\\/-]{1,260}$");

    private PortableBackupCommands() {
    }

    public static List<String> backup(String command, String host, String database, String retention, Path output) {
        requireCommand(command);
        requireHost(host);
        InfluxQl.requireIdent(database);
        InfluxQl.requireIdent(retention);
        return List.of(command, "backup", "-portable", "-database", database,
                "-retention", retention, "-host", host, output.toString());
    }

    public static List<String> restore(String command, String host, String database, String retention,
                                       String targetDatabase, Path input) {
        requireCommand(command);
        requireHost(host);
        InfluxQl.requireIdent(database);
        InfluxQl.requireIdent(retention);
        InfluxQl.requireIdent(targetDatabase);
        if (database.equals(targetDatabase)) {
            throw new IllegalArgumentException("在线恢复不允许覆盖正在使用的库，请指定一个新的库名");
        }
        return List.of(command, "restore", "-portable", "-db", database, "-rp", retention,
                "-newdb", targetDatabase, "-host", host, input.toString());
    }

    public static String defaultTarget(String database) {
        return database + "_restore";
    }

    private static void requireHost(String host) {
        if (host == null || !HOST.matcher(host).matches()) {
            throw new IllegalArgumentException("backup-host 只允许字母、数字、点、冒号、下划线和中划线");
        }
    }

    private static void requireCommand(String command) {
        if (command == null || command.contains("..") || !COMMAND.matcher(command).matches()) {
            throw new IllegalArgumentException("influxd 命令路径不合法");
        }
    }
}
