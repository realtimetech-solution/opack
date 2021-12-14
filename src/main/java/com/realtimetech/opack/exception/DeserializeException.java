package com.realtimetech.opack.exception;

public class DeserializeException extends Exception {
    public DeserializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializeException(String message) {
        super(message);
    }
}
