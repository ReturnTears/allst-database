package com.allst.db.guide;

import com.allst.db.guide.catalog.CypherCatalog;
import com.allst.db.guide.catalog.NamedReadQuery;
import com.allst.db.guide.catalog.QueryParameters;
import com.allst.db.guide.client.CypherRowSet;
import com.allst.db.guide.client.CypherStatement;
import com.allst.db.guide.client.Neo4jException;
import com.allst.db.guide.client.Neo4jResultParser;
import com.allst.db.guide.client.Neo4jUrls;
import com.allst.db.guide.schema.GraphSchemaService;
import com.allst.db.guide.schema.SchemaCommand;
import com.allst.db.guide.schema.SchemaScripts;
import com.allst.db.guide.seed.GraphSeedStatements;
import com.allst.db.guide.seed.GuideLabels;
import com.allst.db.guide.support.LuceneQuerySanitizer;
import com.allst.db.guide.support.PlanFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideCypherRulesTest {

    private static final Pattern PARAMS = Pattern.compile("\\$([A-Za-z][A-Za-z0-9_]*)");
    private static final Pattern WRITES = Pattern.compile("\\b(CREATE|MERGE|DELETE|DETACH|SET|REMOVE|DROP|FOREACH|LOAD)\\b");

    @Test
    void readCatalogUsesParametersAndRejectsWrites() {
        CypherCatalog catalog = new CypherCatalog();
        assertFalse(catalog.all().isEmpty());
        Set<String> names = new HashSet<>();
        for (NamedReadQuery query : catalog.all()) {
            assertTrue(names.add(query.getName()));
            assertTrue(QueryParameters.categories().contains(query.getCategory()));
            assertFalse(WRITES.matcher(query.getCypher()).find(), query.getName());
            Set<String> declared = query.getDefaultParams().keySet();
            Matcher matcher = PARAMS.matcher(query.getCypher());
            while (matcher.find()) {
                assertTrue(declared.contains(matcher.group(1)), query.getName() + " 缺少参数 " + matcher.group(1));
            }
        }
        assertTrue(catalog.find("fulltext-document").isPresent());
        assertEquals("keyword", catalog.find("fulltext-document").get().getFulltextParam());
    }

    @Test
    void queryParametersKeepDefaultsAndCoerceNumbers() {
        NamedReadQuery query = new CypherCatalog().find("syntax-page").get();
        Map<String, String> raw = new HashMap<>();
        raw.put("limit", "4");
        raw.put("skip", "");
        raw.put("injected", "MATCH (n) DETACH DELETE n");
        Map<String, Object> merged = QueryParameters.merge(query, raw);
        assertEquals(4, merged.get("limit"));
        assertEquals(0, merged.get("skip"));
        assertFalse(merged.containsKey("injected"));
        raw.put("limit", "nope");
        assertThrows(IllegalArgumentException.class, () -> QueryParameters.merge(query, raw));
    }

    @Test
    void schemaScriptsStayIdempotentAndVersionAware() {
        List<SchemaCommand> neo4j4 = SchemaScripts.initCommands(4);
        List<SchemaCommand> neo4j5 = SchemaScripts.initCommands(5);
        assertEquals(neo4j4.size(), neo4j5.size());
        for (int i = 0; i < neo4j4.size(); i++) {
            assertEquals(neo4j4.get(i).getName(), neo4j5.get(i).getName());
            assertTrue(neo4j4.get(i).getName().startsWith("guide_"));
            assertTrue(neo4j4.get(i).getCypher().contains(neo4j4.get(i).getName()));
        }
        String constraints4 = neo4j4.get(0).getCypher();
        String constraints5 = neo4j5.get(0).getCypher();
        assertTrue(constraints4.contains("ASSERT"));
        assertFalse(constraints4.contains("REQUIRE"));
        assertTrue(constraints5.contains("REQUIRE"));
        assertFalse(constraints5.contains("ASSERT"));
        assertTrue(neo4j4.stream().anyMatch(command -> command.getCypher().contains("`fulltext.analyzer`: 'cjk'")));
        assertTrue(neo4j4.stream().anyMatch(command -> !command.isRequired()));

        Set<String> dropped = new HashSet<>();
        for (SchemaCommand command : SchemaScripts.dropCommands()) {
            dropped.add(command.getName());
            assertTrue(command.getCypher().contains("IF EXISTS"));
        }
        for (SchemaCommand command : neo4j4) {
            assertTrue(dropped.contains(command.getName()));
        }
    }

    @Test
    void seedOnlyPurgesGuideLabels() {
        List<CypherStatement> statements = GraphSeedStatements.build();
        assertEquals(GuideLabels.PURGE, statements.get(0).getStatement());
        assertFalse(GuideLabels.PURGE.contains("Person"));
        assertFalse(GuideLabels.PURGE.contains("Book"));
        assertTrue(statements.stream().anyMatch(statement -> statement.getStatement().contains("UNWIND $rows")));
        assertTrue(statements.stream().anyMatch(statement -> statement.getStatement().contains("MERGE")));
    }

    @Test
    void luceneEscapeKeepsChineseAndEscapesOperators() {
        assertEquals("索引", LuceneQuerySanitizer.escape(" 索引 "));
        assertEquals("a\\+b", LuceneQuerySanitizer.escape("a+b"));
        assertThrows(IllegalArgumentException.class, () -> LuceneQuerySanitizer.escape(" "));
    }

    @Test
    void parserReadsRowsPlanAndConstraintErrors() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<CypherRowSet> rowSets = Neo4jResultParser.parse(mapper.readTree("{"
                + "\"results\":[{\"columns\":[\"email\",\"skills\"],\"data\":[{\"row\":[\"chen@allst.dev\",[\"Neo4j\"]]}],"
                + "\"stats\":{\"nodes_created\":0},\"plan\":{\"root\":{\"operatorType\":\"ProduceResults\",\"DbHits\":0,"
                + "\"children\":[{\"operatorType\":\"NodeUniqueIndexSeek\",\"DbHits\":2,\"args\":{\"Details\":\"m.email\"},"
                + "\"children\":[]}]}}}],\"errors\":[]}"), 12L);
        assertEquals("chen@allst.dev", rowSets.get(0).getRows().get(0).get("email"));
        assertEquals(Arrays.asList("Neo4j"), rowSets.get(0).getRows().get(0).get("skills"));
        List<Map<String, Object>> operators = PlanFormatter.operators(rowSets.get(0).getPlan());
        assertEquals("NodeUniqueIndexSeek", operators.get(1).get("operatorType"));
        assertEquals("m.email", operators.get(1).get("details"));
        assertEquals(2L, PlanFormatter.totalDbHits(operators));

        Neo4jException error = assertThrows(Neo4jException.class, () -> Neo4jResultParser.parse(mapper.readTree(
                "{\"results\":[],\"errors\":[{\"code\":\"Neo.ClientError.Schema.ConstraintValidationFailed\","
                        + "\"message\":\"Node already exists\"}]}"), 1L));
        assertEquals(409, error.getHttpStatus());
    }

    @Test
    void explainWithoutDbHitsHasNoTotal() {
        Map<String, Object> operator = new LinkedHashMap<>();
        operator.put("operatorType", "NodeByLabelScan");
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("root", operator);
        assertNull(PlanFormatter.totalDbHits(PlanFormatter.operators(plan)));
    }

    @Test
    void transactionIdAndMajorVersion() {
        assertEquals("16", Neo4jUrls.transactionId("http://localhost:7474/db/neo4j/tx/16/commit"));
        assertEquals("7", Neo4jUrls.transactionId("/db/neo4j/tx/7"));
        assertEquals(4, GraphSchemaService.majorVersion("4.3.6"));
        assertEquals(5, GraphSchemaService.majorVersion("5.16.0"));
        assertEquals(0, GraphSchemaService.majorVersion("unknown"));
        assertEquals(Collections.singleton("16"), Collections.singleton(Neo4jUrls.transactionId(
                "http://127.0.0.1:7474/db/neo4j/tx/16/commit")));
    }
}
