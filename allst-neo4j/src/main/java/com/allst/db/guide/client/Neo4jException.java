package com.allst.db.guide.client;

public class Neo4jException extends RuntimeException {

    private final int httpStatus;
    private final String code;

    public Neo4jException(int httpStatus, String code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public static Neo4jException fromCypher(String code, String message) {
        int status = 400;
        if (code != null && code.contains("ConstraintValidationFailed")) {
            status = 409;
        }
        return new Neo4jException(status, code, message);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }
}
