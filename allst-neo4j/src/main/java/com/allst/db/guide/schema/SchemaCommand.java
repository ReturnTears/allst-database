package com.allst.db.guide.schema;

public final class SchemaCommand {

    private final String name;
    private final String cypher;
    private final boolean required;
    private final boolean constraint;

    public SchemaCommand(String name, String cypher, boolean required, boolean constraint) {
        this.name = name;
        this.cypher = cypher;
        this.required = required;
        this.constraint = constraint;
    }

    public String getName() {
        return name;
    }

    public String getCypher() {
        return cypher;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isConstraint() {
        return constraint;
    }
}
