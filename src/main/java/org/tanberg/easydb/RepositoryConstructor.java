package org.tanberg.easydb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates that a constructor is intended for use through reflection, so that we can
 * automatically create any repositories that are needed for fields stored in another
 * table than their declaring class or similar
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepositoryConstructor {

    /**
     * @return The values we need to pass to this constructor, in the same order as the
     * constructor takes them
     */
    RepositoryOption[] value();
}
