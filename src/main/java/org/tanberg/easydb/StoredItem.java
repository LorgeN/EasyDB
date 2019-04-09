package org.tanberg.easydb;

/**
 * An item that can be stored in a database
 */
public interface StoredItem {

    /**
     * Called before values are injected into object instance.
     */
    default void preInject() {
    }

    /**
     * Called after values are injected into object instance.
     */
    default void postInject() {
    }

    /**
     * Called before the object is saved.
     */
    default void preSave() {
    }

    /**
     * Called after the object is saved.
     */
    default void postSave() {
    }

    /**
     * Called before the object is deleted.
     */
    default void preDelete() {
    }

    /**
     * Called after the object is deleted.
     */
    default void postDelete() {
    }
}
