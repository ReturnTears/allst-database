package com.allst.dynamic.scenario.common;

import lombok.Data;

/**
 * 场景接口的统一返回。成功时 code 为 0。
 */
@Data
public class ScenarioResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ScenarioResponse<T> ok(T data) {
        ScenarioResponse<T> response = new ScenarioResponse<>();
        response.setCode(0);
        response.setMessage("ok");
        response.setData(data);
        return response;
    }

    public static <T> ScenarioResponse<T> fail(int code, String message) {
        ScenarioResponse<T> response = new ScenarioResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
