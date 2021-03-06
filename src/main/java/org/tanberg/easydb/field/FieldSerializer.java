package org.tanberg.easydb.field;

import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.util.reflection.UtilType;

/**
 * Features useful utility methods for taking a value from a declared field and
 * converting it to a storable format, using a {@link String text} representation.
 */
public interface FieldSerializer {

    /**
     * Tells us if this constants {@link #toString(ItemRepository, PersistentField, Object)}
     * returns a primitive value. We use this method as the actual return type of the method
     * is always a string, and we have no way to figure out what the actual type is.
     *
     * @param manager The {@link ItemRepository manager}
     * @param field   The {@link PersistentField field}
     * @return If this constants {@link #toString(ItemRepository, PersistentField, Object)}
     * returns a primitive value.
     * @see UtilType#PRIMITIVE_TYPES
     */
    boolean returnsPrimitive(ItemRepository<?> manager, PersistentField<?> field);

    /**
     * Converts the given arguments to a string representation to be stored in a database.
     *
     * @param manager The {@link ItemRepository manager}
     * @param field   The {@link PersistentField field}
     * @param val     The value we wish to save
     * @return The string representation
     */
    String toString(ItemRepository<?> manager, PersistentField<?> field, Object val);

    /**
     * Takes a converted value from {@link #toString(ItemRepository, PersistentField, Object)}
     * and converts it back into the {@link Object} value.
     *
     * @param manager The {@link ItemRepository manager}
     * @param field   The {@link PersistentField field}
     * @param string  The string representation of the value
     * @return The new object created from the string
     */
    Object fromString(ItemRepository<?> manager, PersistentField<?> field, String string);
}
