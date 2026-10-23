package com.allst.boot.support;

import com.allst.boot.exception.PrestoQueryException;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Demo 层的 SQL 约束：单条语句，只读模式下只放行查询类语句。
 * 这不是权限系统，生产环境仍应使用 Presto 的只读账号和访问控制。
 *
 * @author Hutu
 * @since 2026-10-23
 */
public final class PrestoSqlGuard {

    private static final Set<String> READ_KEYWORDS = Set.of(
            "SELECT", "WITH", "SHOW", "DESCRIBE", "DESC", "EXPLAIN");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private PrestoSqlGuard() {
    }

    public static String check(String sql, boolean readOnly) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new PrestoQueryException("SQL 不能为空");
        }
        String cleaned = stripComments(sql).trim();
        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        if (cleaned.isEmpty()) {
            throw new PrestoQueryException("SQL 不能为空");
        }
        if (containsSeparator(cleaned)) {
            throw new PrestoQueryException("不支持一次执行多条 SQL");
        }
        if (readOnly) {
            String keyword = firstKeyword(cleaned);
            if (!READ_KEYWORDS.contains(keyword)) {
                throw new PrestoQueryException("当前为只读模式，仅允许 SELECT、WITH、SHOW、DESCRIBE、EXPLAIN");
            }
        }
        return cleaned;
    }

    public static String requireIdentifier(String value, String label) {
        if (value == null || !IDENTIFIER.matcher(value).matches()) {
            throw new PrestoQueryException(label + " 只允许字母、数字和下划线，且不能以数字开头");
        }
        return value;
    }

    private static String firstKeyword(String sql) {
        int length = sql.length();
        int start = 0;
        while (start < length && Character.isWhitespace(sql.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < length && !Character.isWhitespace(sql.charAt(end))) {
            end++;
        }
        if (start == end) {
            throw new PrestoQueryException("SQL 不能为空");
        }
        return sql.substring(start, end).toUpperCase(Locale.ROOT);
    }

    private static boolean containsSeparator(String sql) {
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';
            if (!inDouble && current == '\'') {
                if (inSingle && next == '\'') {
                    i++;
                    continue;
                }
                inSingle = !inSingle;
                continue;
            }
            if (!inSingle && current == '"') {
                if (inDouble && next == '"') {
                    i++;
                    continue;
                }
                inDouble = !inDouble;
                continue;
            }
            if (!inSingle && !inDouble && current == ';') {
                return true;
            }
        }
        return false;
    }

    private static String stripComments(String sql) {
        StringBuilder out = new StringBuilder(sql.length());
        boolean inSingle = false;
        boolean inDouble = false;
        int i = 0;
        while (i < sql.length()) {
            char current = sql.charAt(i);
            char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';
            if (!inDouble && current == '\'') {
                out.append(current);
                if (inSingle && next == '\'') {
                    out.append(next);
                    i += 2;
                    continue;
                }
                inSingle = !inSingle;
                i++;
                continue;
            }
            if (!inSingle && current == '"') {
                out.append(current);
                if (inDouble && next == '"') {
                    out.append(next);
                    i += 2;
                    continue;
                }
                inDouble = !inDouble;
                i++;
                continue;
            }
            if (!inSingle && !inDouble && current == '-' && next == '-') {
                i += 2;
                while (i < sql.length() && sql.charAt(i) != '\n') {
                    i++;
                }
                out.append(' ');
                continue;
            }
            if (!inSingle && !inDouble && current == '/' && next == '*') {
                int end = sql.indexOf("*/", i + 2);
                if (end < 0) {
                    throw new PrestoQueryException("SQL 注释未闭合");
                }
                i = end + 2;
                out.append(' ');
                continue;
            }
            out.append(current);
            i++;
        }
        return out.toString();
    }
}
