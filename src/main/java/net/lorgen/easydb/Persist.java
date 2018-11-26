package net.lorgen.easydb;

import net.lorgen.easydb.field.FieldSerializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Marks that a field in a {@link StoredItem stored item} is persistent.
 * <p>
 * We could use the {@code transient} keyword to mark excluded fields, but
 * because we wish to have the option to easily provide certain information
 * along with the field, this approach is more suitable.
 *
 * @see StorageKey
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Persist {

    /**
     * @return The name of this field. If left empty, the name of the annotated
     * {@link Field field} will be used.
     * @see StringUtils#isEmpty(CharSequence)
     */
    String name() default "";

    /**
     * @return The {@link DataType type} of data we store in this field. If left
     * as default (or set to {@link DataType#AUTO}) an attempt will be made to
     * resolve a type for the field.
     */
    DataType type() default DataType.AUTO;

    /**
     * @return The size (or length) of this field. Mostly relevant for SQL
     * purposes in which substantially long strings are used.
     */
    int size() default 16;

    /**
     * @return Any type parameters given to the field, e. g. if this field is a
     * {@link Map} or a {@link List}.
     */
    Class<?>[] typeParams() default {};

    Class<? extends FieldSerializer> serializer() default DataType.class;
}
