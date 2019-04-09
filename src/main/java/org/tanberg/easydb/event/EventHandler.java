package org.tanberg.easydb.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    /**
     * @return The priority of this event handler method. The lower
     * this number is, the earlier in the event call will this method
     * be called
     */
    int priority() default 50;
}
