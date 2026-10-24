package com.allst.boot.influx.common;

public class InfluxAccessException extends RuntimeException {

    private final boolean unavailable;

    public InfluxAccessException(String message, Throwable cause, boolean unavailable) {
        super(message, cause);
        this.unavailable = unavailable;
    }

    public boolean isUnavailable() {
        return unavailable;
    }
}
