package com.allst.db.guide.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Neo4j 4.3 使用 ASSERT 约束语法；Neo4j 5 改为 REQUIRE。索引与全文索引两边都能执行。
 * 说明见 docs/neo4j/04-indexes.md、05-fulltext-index.md、06-constraints.md。
 */
public final class SchemaScripts {

    private SchemaScripts() {
    }

    public static List<SchemaCommand> initCommands(int major) {
        int version = major >= 5 ? 5 : 4;
        List<SchemaCommand> commands = new ArrayList<>();
        commands.add(constraint("guide_member_email_unique", unique(version, "guide_member_email_unique", "m", "Member", "email")));
        commands.add(constraint("guide_department_code_unique", unique(version, "guide_department_code_unique", "d", "Department", "code")));
        commands.add(constraint("guide_project_code_unique", unique(version, "guide_project_code_unique", "p", "Project", "code")));
        commands.add(constraint("guide_task_code_unique", unique(version, "guide_task_code_unique", "t", "Task", "code")));
        commands.add(constraint("guide_document_code_unique", unique(version, "guide_document_code_unique", "d", "Document", "code")));
        commands.add(constraint("guide_skill_name_unique", unique(version, "guide_skill_name_unique", "s", "Skill", "name")));
        commands.add(constraint("guide_tag_name_unique", unique(version, "guide_tag_name_unique", "t", "Tag", "name")));
        commands.add(index("guide_member_name", "CREATE INDEX guide_member_name IF NOT EXISTS FOR (m:Member) ON (m.name)"));
        commands.add(index("guide_project_status", "CREATE INDEX guide_project_status IF NOT EXISTS FOR (p:Project) ON (p.status)"));
        commands.add(index("guide_task_project_status",
                "CREATE INDEX guide_task_project_status IF NOT EXISTS FOR (t:Task) ON (t.projectCode, t.status)"));
        commands.add(index("guide_document_updated",
                "CREATE INDEX guide_document_updated IF NOT EXISTS FOR (d:Document) ON (d.updatedAt)"));
        commands.add(index("guide_works_on_role",
                "CREATE INDEX guide_works_on_role IF NOT EXISTS FOR ()-[r:WORKS_ON]-() ON (r.role)"));
        commands.add(index("guide_document_text", fulltextNode(
                "guide_document_text", "Document", "n.title, n.summary, n.content")));
        commands.add(index("guide_task_text", fulltextNode(
                "guide_task_text", "Task", "n.title, n.description")));
        commands.add(index("guide_knows_note",
                "CREATE FULLTEXT INDEX guide_knows_note IF NOT EXISTS FOR ()-[r:KNOWS]-() ON EACH [r.note] "
                        + "OPTIONS {indexConfig: {`fulltext.analyzer`: 'cjk'}}"));
        commands.add(index("guide_node_label_lookup",
                "CREATE LOOKUP INDEX guide_node_label_lookup IF NOT EXISTS FOR (n) ON EACH labels(n)"));
        commands.add(index("guide_rel_type_lookup",
                "CREATE LOOKUP INDEX guide_rel_type_lookup IF NOT EXISTS FOR ()-[r]-() ON EACH type(r)"));
        commands.add(optionalConstraint("guide_member_name_exists", exists(version)));
        return Collections.unmodifiableList(commands);
    }

    public static List<SchemaCommand> dropCommands() {
        List<SchemaCommand> commands = new ArrayList<>();
        for (SchemaCommand command : initCommands(4)) {
            String verb = command.isConstraint() ? "DROP CONSTRAINT " : "DROP INDEX ";
            commands.add(new SchemaCommand(command.getName(), verb + command.getName() + " IF EXISTS", true, command.isConstraint()));
        }
        return Collections.unmodifiableList(commands);
    }

    private static String unique(int version, String name, String variable, String label, String property) {
        if (version >= 5) {
            return "CREATE CONSTRAINT " + name + " IF NOT EXISTS FOR (" + variable + ":" + label
                    + ") REQUIRE " + variable + "." + property + " IS UNIQUE";
        }
        return "CREATE CONSTRAINT " + name + " IF NOT EXISTS ON (" + variable + ":" + label
                + ") ASSERT " + variable + "." + property + " IS UNIQUE";
    }

    private static String exists(int version) {
        if (version >= 5) {
            return "CREATE CONSTRAINT guide_member_name_exists IF NOT EXISTS FOR (m:Member) REQUIRE m.name IS NOT NULL";
        }
        return "CREATE CONSTRAINT guide_member_name_exists IF NOT EXISTS ON (m:Member) ASSERT m.name IS NOT NULL";
    }

    private static String fulltextNode(String name, String label, String properties) {
        return "CREATE FULLTEXT INDEX " + name + " IF NOT EXISTS FOR (n:" + label + ") ON EACH [" + properties + "] "
                + "OPTIONS {indexConfig: {`fulltext.analyzer`: 'cjk'}}";
    }

    private static SchemaCommand constraint(String name, String cypher) {
        return new SchemaCommand(name, cypher, true, true);
    }

    private static SchemaCommand optionalConstraint(String name, String cypher) {
        return new SchemaCommand(name, cypher, false, true);
    }

    private static SchemaCommand index(String name, String cypher) {
        return new SchemaCommand(name, cypher, true, false);
    }
}
