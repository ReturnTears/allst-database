package com.allst.trino.exception;

import com.allst.trino.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Hutu
 * @since 2026-10-23
 */
@RestControllerAdvice
public class TrinoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TrinoExceptionHandler.class);

    @ExceptionHandler(TrinoQueryException.class)
    public ResponseEntity<ApiResponse<Void>> badRequest(TrinoQueryException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> missingParameter(MissingServletRequestParameterException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> unreadable(HttpMessageNotReadableException exception) {
        log.debug("无法读取请求体", exception);
        return ResponseEntity.badRequest().body(ApiResponse.fail("请求体不是合法的 JSON"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> dataAccess(DataAccessException exception) {
        log.error("Trino 访问失败", exception);
        Throwable cause = exception.getMostSpecificCause();
        String message = cause.getMessage() == null ? "Trino 访问失败" : cause.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ApiResponse.fail(message));
    }
}
