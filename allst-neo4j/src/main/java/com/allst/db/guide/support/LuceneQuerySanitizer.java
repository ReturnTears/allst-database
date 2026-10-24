package com.allst.db.guide.support;

/**
 * 全文索引使用 Lucene 查询语法。业务接口默认转义特殊字符，避免用户输入改变查询结构。
 */
public final class LuceneQuerySanitizer {

    private static final String SPECIALS = "+-!():^[]\"{}~*?\\/|&";
    private static final int MAX_LENGTH = 80;

    private LuceneQuerySanitizer() {
    }

    public static String escape(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("检索词不能为空");
        }
        String text = raw.trim();
        if (text.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("检索词最长 " + MAX_LENGTH + " 个字符");
        }
        StringBuilder escaped = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (SPECIALS.indexOf(current) >= 0) {
                escaped.append('\\');
            }
            escaped.append(current);
        }
        return escaped.toString();
    }
}
