package com.allst.boot.influx.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "com.allst.boot")
public class InfluxExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(InfluxExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(400, ex.getMessage()));
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Object>> badParameter(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(400, "请求参数不正确"));
    }

    @ExceptionHandler(InfluxAccessException.class)
    public ResponseEntity<ApiResponse<Object>> influx(InfluxAccessException ex) {
        if (ex.isUnavailable()) {
            log.warn("InfluxDB 不可用: {}", ex.getMessage());
            return ResponseEntity.status(503).body(ApiResponse.fail(503, ex.getMessage()));
        }
        log.warn("InfluxDB 拒绝请求: {}", ex.getMessage());
        return ResponseEntity.status(502).body(ApiResponse.fail(502, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> unexpected(Exception ex) {
        log.error("接口处理失败", ex);
        return ResponseEntity.status(500).body(ApiResponse.fail(500, "服务处理失败"));
    }
}
