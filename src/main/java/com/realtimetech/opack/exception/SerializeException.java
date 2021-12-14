package com.realtimetech.opack.exception;

public class SerializeException extends Exception {
    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializeException(String message) {
        super(message);
    }
}
