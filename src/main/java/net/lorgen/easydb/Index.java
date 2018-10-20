package net.lorgen.easydb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation. Mark a field with this to mark that it is an indexed
 * field, meaning we should be able to execute searches based on a value given
 * to this specific field.
 * <p>
 * Some databases, such as Redis, may require fields to be annotated using this
 * annotation for you to be able to execute queries upon that given field.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {

    /**
     * The ID of this index. If you are looking to execute queries on severalm fields
     * together often, and theses fields are not {@link StorageKey keys}, you can
     * annotate these fields with this annotation and give them the same value. This
     * will create a speed-up on those fields when executing said kinds of queries.
     * <p>
     * If the value is left as default (or set to -1) this field will be left as an
     * index on itself.
     *
     * @return The ID of this index.
     */
    int[] value() default {-1};

    /**
     * @return If any combination of the values in this index is unique (i. e.
     * may only occur once in the entire dataset). Must be set to true for all fields
     * in the index to apply.
     */
    boolean unique() default false;
}
