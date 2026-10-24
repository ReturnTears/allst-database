package com.allst.boot.influx.schema;

public class SchemaStep {

    private final String action;
    private final String status;
    private final String detail;

    public SchemaStep(String action, String status, String detail) {
        this.action = action;
        this.status = status;
        this.detail = detail;
    }

    public String getAction() {
        return action;
    }

    public String getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
