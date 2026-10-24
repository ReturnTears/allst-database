package com.allst.db.guide.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CypherStatement {

    private final String statement;
    private final Map<String, Object> parameters;

    public CypherStatement(String statement, Map<String, Object> parameters) {
        if (statement == null || statement.trim().isEmpty()) {
            throw new IllegalArgumentException("Cypher 不能为空");
        }
        this.statement = statement;
        if (parameters == null || parameters.isEmpty()) {
            this.parameters = Collections.emptyMap();
        } else {
            this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
        }
    }

    public String getStatement() {
        return statement;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
