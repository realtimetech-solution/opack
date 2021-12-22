/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.realtimetech.opack.exception;

public class DeserializeException extends Exception {
    public DeserializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializeException(String message) {
        super(message);
    }

    public DeserializeException(Throwable cause) {
        super(cause);
    }
}
