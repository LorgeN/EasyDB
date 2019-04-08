package net.lorgen.easydb.interact.external;

import net.lorgen.easydb.ItemRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

/**
 * Marks that the value of this field is stored in another, separate
 * {@link ItemRepository repository}.
 * <p>
 * Features special cases for {@link Collection collections} and
 * {@link Map maps}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface External {

    /**
     * @return The table this field's value is stored in
     */
    String table();

    /**
     * @return The class of the {@link ItemRepository} this field's value is
     * stored in
     */
    Class<? extends ItemRepository> repository() default ItemRepository.class;

    /**
     * @return The local field(s) to use as keys when finding external object(s).
     * Names must match in the two stored items. If not set, an attempt to match
     * keys between objects will be made.
     */
    String[] keyFields() default {};

    /**
     * @return If the object(s) in this field are immutable in this context, i. e.
     * if we shouldn't save them when we save this item. Similar to transient fields,
     * only difference being we inject values into these fields
     */
    boolean immutable() default true;
}
