package com.allst.es.scenario.common;

import lombok.Data;

/**
 * 场景接口的统一返回。成功时 code 为 0，失败时与 HTTP 状态一致。
 */
@Data
public class ScenarioResponse<T> {
    private int code;
    private String message;
    private long total;
    private T data;

    public static <T> ScenarioResponse<T> ok(T data) {
        return ok(data, 0L);
    }

    public static <T> ScenarioResponse<T> ok(T data, long total) {
        ScenarioResponse<T> response = new ScenarioResponse<T>();
        response.setCode(0);
        response.setMessage("ok");
        response.setTotal(total);
        response.setData(data);
        return response;
    }

    public static <T> ScenarioResponse<T> fail(int code, String message) {
        ScenarioResponse<T> response = new ScenarioResponse<T>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
