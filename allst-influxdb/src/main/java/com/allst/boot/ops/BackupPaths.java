package com.allst.boot.ops;

import java.nio.file.Path;

public final class BackupPaths {

    private BackupPaths() {
    }

    public static Path resolveFile(Path root, String fileName) {
        if (fileName == null || fileName.contains("..") || !fileName.matches("[A-Za-z0-9._-]{1,128}")
                || !fileName.endsWith(".lp")) {
            throw new IllegalArgumentException("备份文件名只允许字母、数字、点、下划线、中划线，并且以 .lp 结尾");
        }
        return resolve(root, fileName);
    }

    public static Path resolveDirectory(Path root, String directoryName) {
        if (directoryName == null || directoryName.contains("..")
                || !directoryName.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new IllegalArgumentException("备份目录名只允许字母、数字、下划线和中划线");
        }
        return resolve(root, directoryName);
    }

    private static Path resolve(Path root, String child) {
        Path base = root.toAbsolutePath().normalize();
        Path target = base.resolve(child).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("备份路径越界");
        }
        return target;
    }
}
