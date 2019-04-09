package org.tanberg.easydb.exception;

import org.tanberg.easydb.query.Query;

public class SaveQueryException extends RuntimeException {

    public SaveQueryException() {
    }

    public SaveQueryException(String message, Query<?> query) {
        super("An error occurred while running save query " + query + ", message: \"" + message + "\"");
    }

    public SaveQueryException(String message, Throwable cause, Query<?> query) {
        super("An error occurred while running save query " + query + ", message: \"" + message + "\"", cause);
    }

    public SaveQueryException(Query<?> query) {
        super("An error occurred while running save query " + query);
    }

    public SaveQueryException(Throwable cause, Query<?> query) {
        super("An error occurred while running save query " + query, cause);
    }
}
