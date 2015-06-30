package org.jboss.hal.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a GIN module as part of the generated composite GIN module.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface GinModule {
}

