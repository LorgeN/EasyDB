package net.lorgen.easydb.interact;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetFromTable {

    String table();

    boolean saveKeyLocally() default true;

    String keyField() default "";

    boolean updateOnSave() default false;
}
