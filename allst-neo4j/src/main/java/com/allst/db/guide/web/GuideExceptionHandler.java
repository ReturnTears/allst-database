package com.allst.db.guide.web;

import com.allst.db.guide.client.Neo4jException;
import com.allst.db.guide.support.NotFoundException;
import com.allst.db.guide.support.Params;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice(basePackages = "com.allst.db.guide")
public class GuideExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(Neo4jException.class)
    public ResponseEntity<ApiResponse<Object>> neo4j(Neo4jException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(new ApiResponse<>(false, ex.getMessage(),
                ex.getCode() == null ? null : Params.of("code", ex.getCode())));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse<Object>> offline(ResourceAccessException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiResponse<>(false,
                "无法连接 Neo4j HTTP，请确认 7474 端口、数据库名和账号密码。 " + ex.getMessage(), null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "请求体不是合法 JSON", null));
    }
}
