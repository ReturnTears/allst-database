package com.allst.dynamic.scenario.common;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 只覆盖场景包，避免影响旧的 /city 接口。
 */
@RestControllerAdvice(basePackages = "com.allst.dynamic.scenario")
public class ScenarioExceptionHandler {

    @ExceptionHandler(ScenarioException.class)
    public ResponseEntity<ScenarioResponse<Void>> handleScenario(ScenarioException exception) {
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ScenarioResponse.fail(exception.getHttpStatus(), exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ScenarioResponse<Void>> handleUnreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(ScenarioResponse.fail(400, "请求体不是合法 JSON"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ScenarioResponse<Void>> handleType(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(ScenarioResponse.fail(400, exception.getName() + "参数类型不正确"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ScenarioResponse<Void>> handleDataAccess(DataAccessException exception) {
        String message = "数据源不可用。请确认 MySQL 已启动，并已执行 docs/datasource-scenarios/sql/mysql-init.sql。原因: "
                + rootMessage(exception);
        return ResponseEntity.status(503).body(ScenarioResponse.fail(503, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ScenarioResponse<Void>> handleAny(Exception exception) {
        return ResponseEntity.status(500).body(ScenarioResponse.fail(500, rootMessage(exception)));
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
