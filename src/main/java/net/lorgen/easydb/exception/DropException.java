package net.lorgen.easydb.exception;

public class DropException extends RuntimeException {

    public DropException() {
    }

    public DropException(String message) {
        super(message);
    }

    public DropException(String message, Throwable cause) {
        super(message, cause);
    }

    public DropException(Throwable cause) {
        super(cause);
    }

    public DropException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
