package net.lorgen.easydb.exception;

import net.lorgen.easydb.query.Query;

public class SaveQueryException extends RuntimeException {

    public SaveQueryException() {
    }

    public SaveQueryException(String message, Query<?> query) {
        super("An error occurred while running delete query " + query + ", message: \"" + message + "\"");
    }

    public SaveQueryException(String message, Throwable cause, Query<?> query) {
        super("An error occurred while running delete query " + query + ", message: \"" + message + "\"", cause);
    }

    public SaveQueryException(Query<?> query) {
        super("An error occurred while running delete query " + query);
    }

    public SaveQueryException(Throwable cause, Query<?> query) {
        super("An error occurred while running delete query " + query, cause);
    }
}
