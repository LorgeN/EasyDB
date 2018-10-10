package net.lorgen.easydb.exception;

import net.lorgen.easydb.query.Query;

public class DeleteQueryException extends RuntimeException {

    public DeleteQueryException() {
    }

    public DeleteQueryException(String message, Query<?> query) {
        super("An error occurred while running delete query " + query + ", message: \"" + message + "\"");
    }

    public DeleteQueryException(String message, Throwable cause, Query<?> query) {
        super("An error occurred while running delete query " + query + ", message: \"" + message + "\"", cause);
    }

    public DeleteQueryException(Query<?> query) {
        super("An error occurred while running delete query " + query);
    }

    public DeleteQueryException(Throwable cause, Query<?> query) {
        super("An error occurred while running delete query " + query, cause);
    }
}
