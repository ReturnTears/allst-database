package com.allst.boot.ops;

import com.allst.boot.cassandra.common.Identifiers;

import java.nio.file.Path;

public final class BackupPaths {

    private BackupPaths() {
    }

    public static Path root(String configured) {
        if (configured == null || configured.isBlank()) {
            throw new IllegalArgumentException("backup-dir 不能为空");
        }
        Path path = Path.of(configured).toAbsolutePath().normalize();
        if (path.getNameCount() == 0) {
            throw new IllegalArgumentException("backup-dir 不能是根目录");
        }
        return path;
    }

    public static Path resolveFile(Path root, String fileName) {
        if (fileName == null || fileName.contains("..") || !fileName.matches("[A-Za-z0-9._-]{1,128}")
                || !fileName.endsWith(".jsonl")) {
            throw new IllegalArgumentException("备份文件名只允许字母、数字、点、下划线、中划线，并且以 .jsonl 结尾");
        }
        Path base = root.toAbsolutePath().normalize();
        Path target = base.resolve(fileName).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("备份路径越界");
        }
        return target;
    }
}
