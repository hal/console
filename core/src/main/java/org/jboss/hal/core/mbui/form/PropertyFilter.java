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
package org.jboss.hal.core.mbui.form;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class PropertyFilter implements Predicate<Property> {

    private final ModelNodeForm.Builder builder;

    PropertyFilter(final ModelNodeForm.Builder builder) {
        this.builder = builder;
    }

    @Override
    @SuppressWarnings("Guava")
    public boolean apply(final Property property) {
        Predicate<Property> filter;
        Predicate<Property> required = p -> p.getValue().hasDefined(REQUIRED) && p.getValue().get(REQUIRED).asBoolean();
        Predicate<Property> notNillable = p -> p.getValue().hasDefined(NILLABLE) && !p.getValue().get(NILLABLE)
                .asBoolean();

        if (builder.createResource) {
            // if builder.includes isn't empty include required properties plus the ones defined there
            // otherwise include either all or only required properties
            if (builder.includes.isEmpty()) {
                filter = builder.requiredOnly ? required : Predicates.alwaysTrue();
            } else {
                Predicate<Property> included = p -> builder.includes.contains(p.getName());
                filter = Predicates.or(required, included);
            }

        } else {
            if (builder.includes.isEmpty() && builder.excludes.isEmpty()) {
                filter = builder.requiredOnly ? notNillable : Predicates.alwaysTrue();
            } else if (!builder.excludes.isEmpty()) {
                filter = p -> !builder.excludes.contains(p.getName());
            } else {
                filter = p -> builder.includes.contains(p.getName());
            }
            if (!builder.includeRuntime) {
                filter = Predicates.and(filter, p -> !RUNTIME.equals(p.getValue().get(STORAGE).asString()));
            }
        }

        return filter.apply(property);
    }
}
