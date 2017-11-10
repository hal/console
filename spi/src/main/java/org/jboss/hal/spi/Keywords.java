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
 * Search index meta data for proxies. This annotation works as add-on for {@link Requires} which is the
 * primarily source for the search index. Without specifying {@code Keywords} on a proxy, the enclosing presenter
 * is indexed using the defaults specified here. Use {@code Keywords} on a presenter if you want to exclude the
 * presenter or if you want to boost the presenter by setting keywords.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Keywords {

    /**
     * Set of keywords which will be added to the index for that presenter. Using keywords you can boost the presenter
     * in the search result. Please use sparingly.
     *
     * @return an array with keywords to include in the search index
     */
    String[] value() default {};

    /**
     * Using this flag you can exclude a presenter from the search index.
     *
     * @return whether to exclude the presenter
     */
    boolean exclude() default false;
}
