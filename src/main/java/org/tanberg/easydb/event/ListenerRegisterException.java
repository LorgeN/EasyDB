package org.tanberg.easydb.event;

public class ListenerRegisterException extends RuntimeException {

    public ListenerRegisterException(String message) {
        super(message);
    }

    public ListenerRegisterException(String message, Throwable cause) {
        super(message, cause);
    }
}
