package org.jboss.hal.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the required resources for a proxy and its enclosing presenter.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Requires {

    /**
     * Set of required resource to operate on (addressable privilege) within the presenter
     */
    String[] value();

    /**
     * Recursively parse child resources
     */
    boolean recursive() default true;
}
