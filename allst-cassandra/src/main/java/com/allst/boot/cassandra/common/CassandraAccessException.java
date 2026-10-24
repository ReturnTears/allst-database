package com.allst.boot.cassandra.common;

public class CassandraAccessException extends RuntimeException {

    private final boolean unavailable;

    public CassandraAccessException(boolean unavailable, String message, Throwable cause) {
        super(message, cause);
        this.unavailable = unavailable;
    }

    public boolean isUnavailable() {
        return unavailable;
    }
}
