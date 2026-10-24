package com.allst.boot.common;

/**
 * 业务错误。code 会放到 GraphQL errors.extensions.code，message 给调用方直接展示。
 */
public class ApiException extends RuntimeException {

    private final String code;

    public ApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static ApiException notFound(String message) {
        return new ApiException("NOT_FOUND", message);
    }

    public static ApiException conflict(String message) {
        return new ApiException("CONFLICT", message);
    }

    public static ApiException invalid(String message) {
        return new ApiException("VALIDATION", message);
    }

    public static ApiException illegal(String message) {
        return new ApiException("ILLEGAL_STATE", message);
    }

    public String getCode() {
        return code;
    }
}
