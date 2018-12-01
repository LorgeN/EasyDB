package net.lorgen.easydb.interact.join;

import net.lorgen.easydb.ItemRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows for retrieving a specific field value from another repository, avoiding
 * cases where the same data is stored in multiple places, forcing a larger operation
 * if an update on said data is needed
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Join {


    String table();

    String localField();

    String externalField();

    Class<? extends ItemRepository> repository() default ItemRepository.class;
}
