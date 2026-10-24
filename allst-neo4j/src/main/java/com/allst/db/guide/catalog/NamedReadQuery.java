package com.allst.db.guide.catalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NamedReadQuery {

    private final String name;
    private final String category;
    private final String title;
    private final String cypher;
    private final Map<String, Object> defaultParams;
    private final String fulltextParam;

    private NamedReadQuery(String name, String category, String title, String cypher,
                           Map<String, Object> defaultParams, String fulltextParam) {
        this.name = name;
        this.category = category;
        this.title = title;
        this.cypher = cypher;
        this.defaultParams = Collections.unmodifiableMap(defaultParams);
        this.fulltextParam = fulltextParam;
    }

    public static Builder builder(String name, String category, String title) {
        return new Builder(name, category, title);
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getCypher() {
        return cypher;
    }

    public Map<String, Object> getDefaultParams() {
        return defaultParams;
    }

    public String getFulltextParam() {
        return fulltextParam;
    }

    public static final class Builder {
        private final String name;
        private final String category;
        private final String title;
        private String cypher;
        private final Map<String, Object> defaultParams = new LinkedHashMap<>();
        private String fulltextParam;

        private Builder(String name, String category, String title) {
            this.name = name;
            this.category = category;
            this.title = title;
        }

        public Builder cypher(String cypher) {
            this.cypher = cypher;
            return this;
        }

        public Builder param(String key, Object value) {
            defaultParams.put(key, value);
            return this;
        }

        public Builder fulltextParam(String fulltextParam) {
            this.fulltextParam = fulltextParam;
            return this;
        }

        public NamedReadQuery build() {
            if (name == null || !name.matches("[a-z0-9]+(-[a-z0-9]+)*")) {
                throw new IllegalArgumentException("查询名不合法: " + name);
            }
            if (!QueryParameters.categories().contains(category)) {
                throw new IllegalArgumentException("未知分类: " + category);
            }
            if (title == null || title.trim().isEmpty() || cypher == null || cypher.trim().isEmpty()) {
                throw new IllegalArgumentException("查询缺少标题或语句: " + name);
            }
            if (fulltextParam != null && !defaultParams.containsKey(fulltextParam)) {
                throw new IllegalArgumentException(name + " 的全文参数没有默认值");
            }
            return new NamedReadQuery(name, category, title, cypher, defaultParams, fulltextParam);
        }
    }
}
