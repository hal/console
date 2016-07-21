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
package org.jboss.hal.dmr;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Provider;
import org.jboss.hal.dmr.model.NamedNode;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.stream.Collectors.toList;

/**
 * Static helper methods for dealing with {@link ModelNode}s.
 *
 * @author Harald Pehl
 */
public final class ModelNodeHelper {

    private ModelNodeHelper() {}

    /**
     * Tries to get a deeply nested model node from the specified model node. Nested paths must be separated with "/".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "/"
     *
     * @return The nested node or an empty / undefined model node
     */
    public static ModelNode failSafeGet(final ModelNode modelNode, final String path) {
        ModelNode undefined = new ModelNode();

        if (Strings.emptyToNull(path) != null) {
            Iterable<String> keys = Splitter.on('/').omitEmptyStrings().trimResults().split(path);
            if (!Iterables.isEmpty(keys)) {
                ModelNode context = modelNode;
                for (String key : keys) {
                    if (context.hasDefined(key)) {
                        context = context.get(key);
                    } else {
                        context = undefined;
                        break;
                    }
                }
                return context;
            }
        }

        return undefined;
    }

    public static List<ModelNode> failSafeList(final ModelNode modelNode, final String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asList() : Collections.emptyList();
    }

    public static List<Property> failSafePropertyList(final ModelNode modelNode, final String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asPropertyList() : Collections.emptyList();
    }

    /**
     * Turns a list of properties into a list of named model nodes which contains a {@link
     * ModelDescriptionConstants#NAME} key with the properties name.
     */
    public static List<NamedNode> asNamedNodes(List<Property> properties) {
        return properties.stream().map(NamedNode::new).collect(toList());
    }

    public static <T> T getOrDefault(final ModelNode modelNode, String attribute, Provider<T> provider,
            T defaultValue) {
        T result = defaultValue;
        if (modelNode != null && modelNode.hasDefined(attribute)) {
            try {
                result = provider.get();
            } catch (Throwable t) {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
     * Looks for the specified attribute and tries to convert it to an enum constant using
     * {@code LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNode.get(attribute).asString())}.
     */
    public static <E extends Enum<E>> E asEnumValue(final ModelNode modelNode, final String attribute,
            final Function<String, E> valueOf, final E defaultValue) {
        if (modelNode.hasDefined(attribute)) {
            return asEnumValue(modelNode.get(attribute), valueOf, defaultValue);
        }
        return defaultValue;
    }

    public static <E extends Enum<E>> E asEnumValue(final ModelNode modelNode, final Function<String, E> valueOf,
            final E defaultValue) {
        E value = defaultValue;
        String converted = LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNode.asString());
        try {
            value = valueOf.apply(converted);
        } catch (IllegalArgumentException ignored) {}
        return value;
    }

    /**
     * The reverse operation to {@link #asEnumValue(ModelNode, String, Function, Enum)}.
     */
    public static <E extends Enum<E>> String asAttributeValue(final E enumValue) {
        return UPPER_UNDERSCORE.to(LOWER_HYPHEN, enumValue.name());
    }
}
