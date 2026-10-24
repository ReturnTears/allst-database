package com.allst.trino.exception;

/**
 * 调用方可以修正的查询错误，例如空 SQL、多语句或只读限制。
 *
 * @author Hutu
 * @since 2026-10-23
 */
public class TrinoQueryException extends RuntimeException {

    public TrinoQueryException(String message) {
        super(message);
    }
}
