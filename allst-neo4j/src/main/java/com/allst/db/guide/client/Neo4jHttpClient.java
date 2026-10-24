package com.allst.db.guide.client;

import com.allst.db.guide.config.GuideNeo4jProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Neo4j 4.3 事务 HTTP API。旧模块的 OGM 依赖 Bolt 驱动 1.7，不能再引入 Java Driver 4。
 * 说明见 docs/neo4j/10-spring-integration.md。
 */
@Component
public class Neo4jHttpClient {

    private static final Logger log = LoggerFactory.getLogger(Neo4jHttpClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String transactionBase;
    private final String username;
    private final String password;

    public Neo4jHttpClient(@Qualifier("guideNeo4jRestTemplate") RestTemplate restTemplate,
                           ObjectMapper objectMapper,
                           GuideNeo4jProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.transactionBase = normalize(properties.getHttpUri()) + "/db/" + validateDatabase(properties.getDatabase());
        this.username = properties.getUsername() == null ? "" : properties.getUsername();
        this.password = properties.getPassword() == null ? "" : properties.getPassword();
    }

    public CypherRowSet query(String statement, Map<String, Object> parameters) {
        List<CypherRowSet> results = commitAll(Collections.singletonList(new CypherStatement(statement, parameters)));
        return results.isEmpty() ? CypherRowSet.empty() : results.get(0);
    }

    public List<CypherRowSet> commitAll(List<CypherStatement> statements) {
        return post(transactionBase + "/tx/commit", statements);
    }

    public Neo4jTransaction begin() {
        JsonPayload payload = exchange(HttpMethod.POST, transactionBase + "/tx", Collections.emptyList());
        Neo4jResultParser.parse(payload.body, payload.elapsedMs);
        String id = Neo4jUrls.transactionId(payload.body.path("commit").asText(null));
        if (id == null) {
            throw new Neo4jException(502, "NoTransaction", "Neo4j 没有返回事务编号");
        }
        return new Neo4jTransaction(this, id);
    }

    CypherRowSet runIn(String transactionId, CypherStatement statement) {
        List<CypherRowSet> results = post(transactionUrl(transactionId), Collections.singletonList(statement));
        return results.isEmpty() ? CypherRowSet.empty() : results.get(0);
    }

    void commitOpen(String transactionId) {
        post(transactionUrl(transactionId) + "/commit", Collections.emptyList());
    }

    void rollback(String transactionId) {
        JsonPayload payload = exchange(HttpMethod.DELETE, transactionUrl(transactionId), Collections.emptyList());
        if (payload.body != null) {
            Neo4jResultParser.parse(payload.body, payload.elapsedMs);
        }
    }

    private List<CypherRowSet> post(String url, List<CypherStatement> statements) {
        JsonPayload payload = exchange(HttpMethod.POST, url, statements);
        return Neo4jResultParser.parse(payload.body, payload.elapsedMs);
    }

    private JsonPayload exchange(HttpMethod method, String url, List<CypherStatement> statements) {
        long startedAt = System.nanoTime();
        try {
            HttpEntity<?> entity = method == HttpMethod.DELETE
                    ? new HttpEntity<>(headers(false))
                    : new HttpEntity<>(body(statements), headers(true));
            JsonNode response = restTemplate.exchange(url, method, entity, JsonNode.class).getBody();
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            logCall(statements, elapsedMs);
            return new JsonPayload(response, elapsedMs);
        } catch (HttpStatusCodeException ex) {
            throw httpFailure(ex);
        }
    }

    private ObjectNode body(List<CypherStatement> statements) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode array = root.putArray("statements");
        for (CypherStatement statement : statements) {
            ObjectNode node = array.addObject();
            node.put("statement", statement.getStatement());
            node.set("parameters", objectMapper.valueToTree(statement.getParameters()));
            node.put("includeStats", true);
        }
        return root;
    }

    private HttpHeaders headers(boolean jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        if (jsonBody) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        String token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + token);
        return headers;
    }

    private String transactionUrl(String transactionId) {
        if (transactionId == null || !transactionId.matches("\\d+")) {
            throw new IllegalArgumentException("事务编号不合法");
        }
        return transactionBase + "/tx/" + transactionId;
    }

    private void logCall(List<CypherStatement> statements, long elapsedMs) {
        if (elapsedMs >= 1000) {
            log.warn("Neo4j HTTP 调用耗时 {} ms，语句数 {}", elapsedMs, statements.size());
        } else if (log.isDebugEnabled() && !statements.isEmpty()) {
            log.debug("Neo4j HTTP {} ms: {}", elapsedMs, statements.get(0).getStatement());
        }
    }

    private Neo4jException httpFailure(HttpStatusCodeException ex) {
        String body = new String(ex.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        try {
            JsonNode parsed = objectMapper.readTree(body);
            JsonNode errors = parsed.get("errors");
            if (errors != null && errors.isArray() && errors.size() > 0) {
                String code = errors.get(0).path("code").asText("");
                Neo4jException cypher = Neo4jException.fromCypher(code, errors.get(0).path("message").asText(body));
                if (ex.getRawStatusCode() == 401 || ex.getRawStatusCode() == 403) {
                    return new Neo4jException(ex.getRawStatusCode(), code, cypher.getMessage());
                }
                return cypher;
            }
        } catch (Neo4jException cypher) {
            return cypher;
        } catch (Exception ignored) {
            // 响应体不是 JSON 时使用 HTTP 状态。
        }
        int status = ex.getRawStatusCode() >= 500 ? 503 : ex.getRawStatusCode();
        return new Neo4jException(status, "Http" + ex.getRawStatusCode(), "Neo4j HTTP " + ex.getRawStatusCode());
    }

    private static String normalize(String httpUri) {
        if (httpUri == null || httpUri.trim().isEmpty()) {
            throw new IllegalArgumentException("allst.neo4j.http-uri 不能为空");
        }
        String uri = httpUri.trim();
        if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
            throw new IllegalArgumentException("allst.neo4j.http-uri 必须以 http:// 或 https:// 开头");
        }
        while (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    private static String validateDatabase(String database) {
        if (database == null || !database.matches("[A-Za-z0-9][A-Za-z0-9_.-]{0,62}")) {
            throw new IllegalArgumentException("allst.neo4j.database 不合法");
        }
        return database;
    }

    private static final class JsonPayload {
        private final JsonNode body;
        private final long elapsedMs;

        private JsonPayload(JsonNode body, long elapsedMs) {
            this.body = body;
            this.elapsedMs = elapsedMs;
        }
    }
}
