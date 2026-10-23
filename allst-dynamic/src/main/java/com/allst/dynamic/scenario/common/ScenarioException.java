package com.allst.dynamic.scenario.common;

/**
 * 场景接口的业务错误。httpStatus 同时作为 HTTP 状态和响应体里的 code。
 */
public class ScenarioException extends RuntimeException {

    private final int httpStatus;

    public ScenarioException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
