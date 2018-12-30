package net.lorgen.easydb.exception;

import net.lorgen.easydb.query.Query;

public class FindQueryException extends RuntimeException {

    public FindQueryException() {
    }

    public FindQueryException(String message, Query<?> query) {
        super("An error occurred while running find query " + query + ", message: \"" + message + "\"");
    }

    public FindQueryException(String message, Throwable cause, Query<?> query) {
        super("An error occurred while running find query " + query + ", message: \"" + message + "\"", cause);
    }

    public FindQueryException(Query<?> query) {
        super("An error occurred while running find query " + query);
    }

    public FindQueryException(Throwable cause, Query<?> query) {
        super("An error occurred while running find query " + query, cause);
    }
}
