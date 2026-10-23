package com.allst.boot.support;

import com.allst.boot.exception.PrestoQueryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrestoSqlGuardTest {

    @Test
    void allowsSelectAndDropsTrailingSemicolon() {
        assertEquals("SELECT 1", PrestoSqlGuard.check("  SELECT 1;  ", true));
    }

    @Test
    void allowsLeadingCommentsAndSemicolonInsideString() {
        String sql = "/* head */ -- line\nSELECT ';' AS value";
        assertEquals("SELECT ';' AS value", PrestoSqlGuard.check(sql, true));
    }

    @Test
    void allowsShowWithAndExplain() {
        assertEquals("SHOW CATALOGS", PrestoSqlGuard.check("SHOW CATALOGS", true));
        assertEquals("WITH t AS (SELECT 1) SELECT * FROM t",
                PrestoSqlGuard.check("WITH t AS (SELECT 1) SELECT * FROM t", true));
        assertEquals("EXPLAIN SELECT 1", PrestoSqlGuard.check("EXPLAIN SELECT 1", true));
    }

    @Test
    void rejectsWriteAndMultipleStatementsWhenReadOnly() {
        assertThrows(PrestoQueryException.class, () -> PrestoSqlGuard.check("DELETE FROM tpch.tiny.nation", true));
        assertThrows(PrestoQueryException.class, () -> PrestoSqlGuard.check("SELECT 1; SELECT 2", true));
    }

    @Test
    void allowsWriteWhenReadOnlyDisabled() {
        assertEquals("DELETE FROM tpch.tiny.nation",
                PrestoSqlGuard.check("DELETE FROM tpch.tiny.nation", false));
    }

    @Test
    void rejectsUnclosedCommentAndBlankSql() {
        assertThrows(PrestoQueryException.class, () -> PrestoSqlGuard.check("/* SELECT 1", true));
        assertThrows(PrestoQueryException.class, () -> PrestoSqlGuard.check("   ", true));
    }

    @Test
    void checksIdentifiers() {
        assertEquals("tpch", PrestoSqlGuard.requireIdentifier("tpch", "catalog"));
        assertThrows(PrestoQueryException.class, () -> PrestoSqlGuard.requireIdentifier("tpch.tiny", "catalog"));
        assertThrows(PrestoQueryException.class, () -> PrestoSqlGuard.requireIdentifier("1abc", "schema"));
    }
}
