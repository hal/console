/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.mbui.form;

import java.util.function.Predicate;

import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class PropertyFilter implements Predicate<Property> {

    private final ModelNodeForm.Builder builder;

    PropertyFilter(final ModelNodeForm.Builder builder) {
        this.builder = builder;
    }

    @Override
    public boolean test(final Property property) {
        Predicate<Property> filter;
        // always include add-index
        Predicate<Property> isAddIndex = p -> p.getName().equals(ADD_INDEX);
        Predicate<Property> required = isAddIndex
                .or(p -> ((p.getValue().hasDefined(REQUIRED) && p.getValue().get(REQUIRED).asBoolean()) ||
                        (p.getValue().hasDefined(NILLABLE) && !p.getValue().get(NILLABLE).asBoolean())));

        // do not include "deprecated" attributes
        if (builder.hideDeprecated && property.getValue().hasDefined(DEPRECATED)) {
            return false;
        }

        if (builder.addOnly) {
            // if builder.includes is empty include either all or only required properties
            // otherwise include required properties plus the ones defined in builder.includes
            if (emptyIncludes() && builder.excludes.isEmpty()) {
                filter = builder.requiredOnly ? required : (p) -> true;
            } else if (!builder.excludes.isEmpty()) {
                filter = p -> !builder.excludes.contains(p.getName());
            } else {
                Predicate<Property> included = p -> builder.includes.contains(p.getName());
                filter = required.or(included);
            }

        } else {
            if (emptyIncludes() && builder.excludes.isEmpty()) {
                filter = builder.requiredOnly ? required : (p) -> true;
            } else if (!builder.excludes.isEmpty()) {
                filter = p -> {
                    boolean result = !builder.excludes.contains(p.getName());
                    int pos = p.getName().indexOf('.');
                    if (pos != -1) {
                        String wildcard = p.getName().substring(0, pos) + ".*";
                        result = result && !builder.excludes.contains(wildcard);
                    }
                    return result;
                };
            } else {
                filter = p -> builder.includes.contains(p.getName());
            }
        }
        if (!builder.includeRuntime) {
            filter = filter.and(
                    p -> !p.getValue().hasDefined(STORAGE) || !RUNTIME.equals(p.getValue().get(STORAGE).asString()));
        }

        return filter.test(property);
    }

    private boolean emptyIncludes() {
        if (builder.includes.isEmpty()) {
            return true;
        } else {
            // custom form items are added automatically to builder.includes by
            // org.jboss.hal.core.mbui.form.ModelNodeForm.Builder.customFormItem()
            // that doesn't count when we want to know whether builder.includes is 'empty'
            for (Object include : builder.includes) {
                // if the include is no custom form item, it was added explicitly
                if (!builder.providers.containsKey(String.valueOf(include))) {
                    return false;
                }
            }
            // all includes are custom form items which were added automatically
            // so we can say builder.includes is actually empty
            return true;
        }
    }
}
