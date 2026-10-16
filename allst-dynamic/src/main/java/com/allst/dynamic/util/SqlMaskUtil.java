package com.allst.dynamic.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlMaskUtil {

    public static void main(String[] args) {
        String sql = """
                SELECT * FROM users 
                WHERE name = '张三丰' 
                  AND gender='N'
                  AND col1='"N'
                  AND col2="N"
                  AND age = 25 
                  AND id IN ('10086', '10087', '10088')
                  AND status = 'ACTIVE'
                """;
        System.out.println(maskSql(sql));
    }

    // 匹配 = 'value' 或 = value (非引号)
    private static final Pattern EQ_PATTERN = Pattern.compile(
            "(=\\s*)('(?:[^'\\\\]|\\\\.)*'|\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    // 匹配 IN (...) 括号内的内容
    private static final Pattern IN_PATTERN = Pattern.compile(
            "(\\bIN\\s*\\()([^)]+)(\\))",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * 对SQL WHERE子句中的敏感值进行脱敏（仅用于日志/展示）
     */
    public static String maskSql(String sql) {
        if (sql == null || sql.isBlank()) return sql;

        // 1. 先处理 IN (...)
        /*String masked = IN_PATTERN.matcher(sql).replaceAll(m -> {
            String prefix = m.group(1);  // IN(
            String content = m.group(2); // 括号内原始值
            String suffix = m.group(3);  // )
            return prefix + maskInValues(content) + suffix;
        });*/

        // 2. 再处理 = value
        String masked = EQ_PATTERN.matcher(sql).replaceAll(m -> {
            String eqPart = m.group(1);   // "= "
            String valPart = m.group(2);  // 值（可能带引号）
            return eqPart + maskSingleValue(valPart);
        });

        return masked;
    }

    /** 脱敏单个值，保留首尾字符 */
    private static String maskSingleValue(String value) {
        if (value == null || value.length() <= 2) return "'****'";
        // 去掉外层引号后脱敏，再重新加上引号
        boolean quoted = value.startsWith("'") && value.endsWith("'");
        String inner = quoted ? value.substring(1, value.length() - 1) : value;
        if (inner.length() <= 2) return quoted ? "'****'" : "****";
        String masked = inner.charAt(0) + "****" + inner.charAt(inner.length() - 1);
        return quoted ? "'" + masked + "'" : masked;
    }

    /** 脱敏IN列表中的每个值 */
    private static String maskInValues(String inContent) {
        String[] parts = inContent.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String trimmed = parts[i].trim();
            if (i > 0) sb.append(", ");
            sb.append(maskSingleValue(trimmed));
        }
        return sb.toString();
    }
}
