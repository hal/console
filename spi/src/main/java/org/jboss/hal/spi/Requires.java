/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the required resources attached to an UI element. Currently this is either a presenter-proxy or a finder
 * column. You can prefix a resource with {@value OPTIONAL} to mark it as optional. Optional resources won't
 * throw an exception if they cannot be read.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Requires {

    /**
     * Set of required resource to operate on (addressable privilege)
     */
    String[] value();

    /**
     * Recursively parse child resources
     */
    boolean recursive() default true;
}
