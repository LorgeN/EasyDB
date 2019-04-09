package org.tanberg.easydb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeserializerConstructor {

    /**
     * @return The fields this constructor requires. Case-sensitive.
     */
    String[] value();
}
