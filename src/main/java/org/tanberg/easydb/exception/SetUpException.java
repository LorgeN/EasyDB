package org.tanberg.easydb.exception;

public class SetUpException extends RuntimeException {

    public SetUpException() {
    }

    public SetUpException(String message) {
        super(message);
    }

    public SetUpException(String message, Throwable cause) {
        super(message, cause);
    }

    public SetUpException(Throwable cause) {
        super(cause);
    }

    public SetUpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
