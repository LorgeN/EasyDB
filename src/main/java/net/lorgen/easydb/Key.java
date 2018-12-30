package net.lorgen.easydb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * This annotation infers that the annotated {@link Field field} is
 * a key value. This means that if you combine all annotated fields
 * in a given {@link StoredItem stored item} {@link Class class},
 * give them a value and execute a query upon a database with those
 * given values, only 1 result will ever be returned. In other words,
 * that combination is <b>unique</b>.
 * <p>
 * The field is also <b>required</b> to have the {@link Options}
 * annotation.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Key {

    /**
     * If this field should automatically increment by 1 for every
     * new entry created. This may slow down saving as in some cases
     * multiple queries may be required.
     * <p>
     * The annotated field must be of {@link DataType type}
     * {@link DataType#INTEGER integer}, and be the <b>only</b> field
     * annotated with {@link Key this annotation}.
     *
     * @return If this field should automatically increment by 1 when
     * creating a new entry in the database
     */
    boolean autoIncrement() default false;
}
