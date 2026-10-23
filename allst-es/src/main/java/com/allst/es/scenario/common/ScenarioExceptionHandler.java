package com.allst.es.scenario.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 只处理场景包里的接口，不改变原有练习接口的异常行为。
 */
@RestControllerAdvice(basePackages = "com.allst.es.scenario")
public class ScenarioExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ScenarioExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ScenarioResponse<Void>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ScenarioResponse.<Void>fail(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ScenarioResponse<Void>> failed(Exception ex) {
        log.error("ES场景接口失败", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ScenarioResponse.<Void>fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), rootMessage(ex)));
    }

    private String rootMessage(Throwable ex) {
        Throwable current = ex;
        String message = ex.getMessage();
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
            if (current.getMessage() != null) {
                message = current.getMessage();
            }
        }
        if (message == null || message.trim().isEmpty()) {
            return ex.getClass().getSimpleName();
        }
        return message;
    }
}
