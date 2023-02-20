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
package org.jboss.hal.dmr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_INDEX;

/**
 * Static helper methods for dealing with {@link ModelNode}s and {@link NamedNode}s. Some methods accept a path parameter
 * separated by "/" to get a deeply nested data.
 */
public class ModelNodeHelper {

    private static final String ENCODED_SLASH = "%2F";
    private static final DateTimeFormat ISO_8601 = GWT.isScript() ? DateTimeFormat.getFormat(
            DateTimeFormat.PredefinedFormat.ISO_8601) : null;

    public static String encodeValue(String value) {
        return value.replace("/", ENCODED_SLASH);
    }

    public static String decodeValue(String value) {
        return value.replace(ENCODED_SLASH, "/");
    }

    /**
     * Tries to get a deeply nested model node from the specified model node. Nested paths must be separated with "/".
     *
     * @param modelNode The model node to read from
     * @param path A path separated with "/"
     *
     * @return The nested node or an empty / undefined model node
     */
    public static ModelNode failSafeGet(ModelNode modelNode, String path) {
        ModelNode undefined = new ModelNode();

        if (Strings.emptyToNull(path) != null) {
            Iterable<String> keys = Splitter.on('/').omitEmptyStrings().trimResults().split(path);
            if (!Iterables.isEmpty(keys)) {
                ModelNode context = modelNode;
                for (String key : keys) {
                    String safeKey = decodeValue(key);
                    if (context.hasDefined(safeKey)) {
                        context = context.get(safeKey);
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

    /**
     * Tries to get a deeply nested boolean value from the specified model node. Nested paths must be separated with "/".
     *
     * @param modelNode The model node to read from
     * @param path A path separated with "/"
     *
     * @return the boolean value or false.
     */
    public static boolean failSafeBoolean(ModelNode modelNode, String path) {
        ModelNode attribute = failSafeGet(modelNode, path);
        return attribute.isDefined() && attribute.asBoolean();
    }

    public static Date failSafeDate(ModelNode modelNode, String path) {
        ModelNode attribute = failSafeGet(modelNode, path);
        if (attribute.isDefined()) {
            try {
                String date = attribute.asString();
                if (date.indexOf('[') != -1 && date.endsWith("]")) {
                    // Strip zone ID which comes from using
                    // java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME instead of
                    // java.time.format.DateTimeFormatter.ISO_DATE_TIME
                    // see https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_ZONED_DATE_TIME
                    date = date.substring(0, date.indexOf('['));
                }
                if (date.indexOf('.') == -1) {
                    // dates w/o millis throw IAEs
                    // fix 2023-02-23T19:23:45Z --> 2023-02-23T19:23:45.000Z
                    int lastColon = date.lastIndexOf(':');
                    date = date.substring(0, lastColon) +
                            ":" +
                            date.substring(lastColon + 1, lastColon + 3) +
                            ".000" +
                            date.substring(lastColon + 3);
                }
                return ISO_8601.parse(date);
            } catch (IllegalArgumentException ignore) {
            }
        }
        return null;
    }

    public static List<ModelNode> failSafeList(ModelNode modelNode, String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asList() : Collections.emptyList();
    }

    public static List<Property> failSafePropertyList(ModelNode modelNode, String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asPropertyList() : Collections.emptyList();
    }

    public static <T> T getOrDefault(ModelNode modelNode, String attribute, Supplier<T> supplier, T defaultValue) {
        T result = defaultValue;
        if (modelNode != null && modelNode.hasDefined(attribute)) {
            try {
                result = supplier.get();
            } catch (Throwable ignored) {
                result = defaultValue;
            }
        }
        return result;
    }

    public static <T extends ModelNode> void storeIndex(List<T> modelNodes) {
        int index = 0;
        for (ModelNode modelNode : modelNodes) {
            modelNode.get(HAL_INDEX).set(index);
            index++;
        }
    }

    /**
     * Turns a list of properties into a list of named model nodes which contains a {@link ModelDescriptionConstants#NAME} key
     * with the properties name.
     */
    public static List<NamedNode> asNamedNodes(List<Property> properties) {
        return properties.stream().map(NamedNode::new).collect(toList());
    }

    /**
     * Looks for the specified attribute and tries to convert it to an enum constant using
     * {@code LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNode.get(attribute).asString())}.
     */
    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNode, String attribute, Function<String, E> valueOf,
            E defaultValue) {
        if (modelNode.hasDefined(attribute)) {
            return asEnumValue(modelNode.get(attribute), valueOf, defaultValue);
        }
        return defaultValue;
    }

    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNodeValue, Function<String, E> valueOf, E defaultValue) {
        E value = defaultValue;
        String convertedValue = LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNodeValue.asString());
        try {
            value = valueOf.apply(convertedValue);
        } catch (IllegalArgumentException ignored) {
        }
        return value;
    }

    /**
     * The reverse operation to {@link #asEnumValue(ModelNode, String, Function, Enum)}.
     */
    public static <E extends Enum<E>> String asAttributeValue(E enumValue) {
        return UPPER_UNDERSCORE.to(LOWER_HYPHEN, enumValue.name());
    }

    /** Moves an attribute to another destination. Both source and destination can be a paths. */
    public static void move(ModelNode modelNode, String source, String destination) {
        if (modelNode != null && Strings.emptyToNull(source) != null && Strings.emptyToNull(destination) != null) {
            ModelNode value = null;
            ModelNode context = modelNode;
            List<String> sourceNames = Splitter.on('/')
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(source);
            if (!sourceNames.isEmpty()) {
                for (Iterator<String> iterator = sourceNames.iterator(); iterator.hasNext();) {
                    String name = iterator.next();
                    String safeName = decodeValue(name);
                    if (context.hasDefined(safeName)) {
                        if (iterator.hasNext()) {
                            context = context.get(safeName);
                        } else {
                            value = context.remove(safeName);
                            break;
                        }
                    }
                }
            }
            if (value != null) {
                context = modelNode;
                List<String> destinationNames = Splitter.on('/')
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(destination);
                for (Iterator<String> iterator = destinationNames.iterator(); iterator.hasNext();) {
                    String name = iterator.next();
                    String safeName = decodeValue(name);
                    if (iterator.hasNext()) {
                        context = context.get(safeName);
                    } else {
                        context.get(safeName).set(value);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Turns a list of properties (keys and values) into a model node.
     *
     * @param properties A list of properties with even size.
     *
     * @return a model node with the specified properties.
     */
    public static ModelNode properties(String... properties) {
        ModelNode modelNode = new ModelNode();
        if (properties != null) {
            List<String> p = Lists.newArrayList(properties);
            for (Iterator<String> iterator = p.iterator(); iterator.hasNext();) {
                String key = iterator.next();
                if (iterator.hasNext()) {
                    String value = iterator.next();
                    if (value != null) {
                        modelNode.get(key).set(value);
                    }
                }
            }
        }
        return modelNode;
    }

    /**
     * Turns all properties which contain one or more '.' into nested model nodes.
     */
    public static ModelNode flatToNested(ModelNode modelNode) {
        if (modelNode != null && modelNode.isDefined()) {
            List<Property> dottedProperties = modelNode.asPropertyList().stream()
                    .filter(property -> property.getName().indexOf('.') != -1)
                    .sorted(comparing(Property::getName).reversed())
                    .collect(toList());
            if (!dottedProperties.isEmpty()) {
                for (Property property : dottedProperties) {
                    String name = property.getName();
                    ModelNode value = property.getValue();
                    String[] parts = name.split("\\.");
                    ModelNode check = modelNode.get(parts);
                    if (!check.isDefined()) {
                        check.set(value);
                    }
                }
                List<String> cleanup = new ArrayList<>(dottedProperties.stream().map(Property::getName).collect(toList()));
                for (String name : cleanup) {
                    modelNode.remove(name);
                }
            }
        }
        return modelNode;
    }

    private ModelNodeHelper() {
    }
}
