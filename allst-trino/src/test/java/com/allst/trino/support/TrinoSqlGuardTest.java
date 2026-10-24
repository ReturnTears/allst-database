package com.allst.trino.support;

import com.allst.trino.exception.TrinoQueryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrinoSqlGuardTest {

    @Test
    void allowsSelectAndDropsTrailingSemicolon() {
        assertEquals("SELECT 1", TrinoSqlGuard.check("  SELECT 1;  ", true));
    }

    @Test
    void allowsLeadingCommentsAndSemicolonInsideString() {
        String sql = "/* head */ -- line\nSELECT ';' AS value";
        assertEquals("SELECT ';' AS value", TrinoSqlGuard.check(sql, true));
    }

    @Test
    void allowsShowWithAndExplain() {
        assertEquals("SHOW CATALOGS", TrinoSqlGuard.check("SHOW CATALOGS", true));
        assertEquals("WITH t AS (SELECT 1) SELECT * FROM t",
                TrinoSqlGuard.check("WITH t AS (SELECT 1) SELECT * FROM t", true));
        assertEquals("EXPLAIN SELECT 1", TrinoSqlGuard.check("EXPLAIN SELECT 1", true));
    }

    @Test
    void rejectsWriteAndMultipleStatementsWhenReadOnly() {
        assertThrows(TrinoQueryException.class, () -> TrinoSqlGuard.check("DELETE FROM tpch.tiny.nation", true));
        assertThrows(TrinoQueryException.class, () -> TrinoSqlGuard.check("SELECT 1; SELECT 2", true));
    }

    @Test
    void allowsWriteWhenReadOnlyDisabled() {
        assertEquals("CREATE TABLE memory.default.demo (id integer)",
                TrinoSqlGuard.check("CREATE TABLE memory.default.demo (id integer)", false));
    }

    @Test
    void rejectsUnclosedCommentAndBlankSql() {
        assertThrows(TrinoQueryException.class, () -> TrinoSqlGuard.check("/* SELECT 1", true));
        assertThrows(TrinoQueryException.class, () -> TrinoSqlGuard.check("   ", true));
    }

    @Test
    void checksIdentifiers() {
        assertEquals("tpch", TrinoSqlGuard.requireIdentifier("tpch", "catalog"));
        assertThrows(TrinoQueryException.class, () -> TrinoSqlGuard.requireIdentifier("tpch.tiny", "catalog"));
        assertThrows(TrinoQueryException.class, () -> TrinoSqlGuard.requireIdentifier("1abc", "schema"));
    }
}
