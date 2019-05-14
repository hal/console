/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import elemental2.core.JsArray;
import jsinterop.base.Any;
import jsinterop.base.JsPropertyMap;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.core.Global.JSON;
import static java.util.Collections.emptyList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelType.BIG_INTEGER;
import static org.jboss.hal.dmr.ModelType.INT;

public final class Json {

    private static final String EMPTY_JSON = "Empty JSON '{}'";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(Json.class);

    @SuppressWarnings("unchecked")
    public static List<ModelNode> parseArray(String json, Metadata metadata, Map<String, String> mappping) {
        if (Strings.emptyToNull(json) != null) {
            JsArray<JsPropertyMap<Object>> array = (JsArray<JsPropertyMap<Object>>) JSON.parse(json);
            if (array.length != 0) {
                List<ModelNode> nodes = new ArrayList<>();
                for (int i = 0; i < array.length; i++) {
                    JsPropertyMap<Object> map = array.getAt(i);
                    ModelNode node = iterateMap(map, metadata, mappping);
                    if (node.isDefined()) {
                        nodes.add(node);
                    }
                }
                return nodes;
            } else {
                logger.warn(EMPTY_JSON, json);
                return emptyList();
            }
        } else {
            logger.warn(EMPTY_JSON, json);
            return emptyList();
        }
    }


    @SuppressWarnings("unchecked")
    public static ModelNode parseSingle(String json, Metadata metadata, Map<String, String> mappping) {
        if (Strings.emptyToNull(json) != null) {
            JsPropertyMap<Object> map = (JsPropertyMap<Object>) JSON.parse(json);
            if (map != null) {
                return iterateMap(map, metadata, mappping);
            } else {
                logger.warn(EMPTY_JSON, json);
                return new ModelNode();
            }
        } else {
            logger.warn(EMPTY_JSON, json);
            return new ModelNode();
        }
    }

    private static ModelNode iterateMap(JsPropertyMap<Object> map, Metadata metadata, Map<String, String> mappping) {
        ModelNode node = new ModelNode();
        List<Property> attributeDescriptions = metadata.getDescription().getAttributes(ATTRIBUTES);

        map.forEach(jsonName -> {
            String dmrName = mappping.get(jsonName);
            if (dmrName != null) {
                ModelNode attributeDescription = findAttributeDescription(dmrName, attributeDescriptions);
                if (attributeDescription != null) {
                    if (map.has(jsonName)) {
                        Any any = map.getAny(jsonName);
                        ModelNode value = anyValue(jsonName, dmrName, attributeDescription, any);
                        if (value.isDefined()) {
                            node.get(dmrName).set(value);
                        }
                    }
                } else {
                    logger.warn("No attribute description found for JSON key '{}' / DMR attribute '{}'",
                            jsonName, dmrName);
                }
            } else {
                logger.warn("No mapping from JSON to DMR found for JSON key '{}'", jsonName);
            }
        });
        return node;
    }

    private static ModelNode anyValue(String jsonName, String dmrName, ModelNode attributeDescription,
            Any value) {
        ModelNode node = new ModelNode();
        ModelType type = attributeDescription.get(TYPE).asType();
        switch (type) {
            case BOOLEAN:
                node.set(value.asBoolean());
                break;

            case BIG_INTEGER:
            case INT:
            case LONG:
                long longValue = value.asLong();
                if (type == BIG_INTEGER) {
                    node.set(BigInteger.valueOf(longValue));
                } else if (type == INT) {
                    node.set((int) longValue);
                } else {
                    node.set(longValue);
                }
                break;

            case DOUBLE:
                double doubleValue = value.asDouble();
                node.set(doubleValue);
                break;

            case LIST:
                Any[] array = value.asArray();
                for (Any any : array) {
                    node.add(String.valueOf(any));
                }
                break;

            case OBJECT:
                boolean stringValueType = attributeDescription.get(VALUE_TYPE)
                        .getType()
                        .equals(ModelType.TYPE)
                        && attributeDescription.get(VALUE_TYPE).asType().equals(ModelType.STRING);
                if (stringValueType) {
                    JsPropertyMap<Object> map = value.asPropertyMap();
                    map.forEach(key -> node.get(key).set(String.valueOf(map.getAny(key))));
                }
                break;


            case STRING:
                node.set(value.asString());
                break;

            // unsupported types
            case BIG_DECIMAL:
            case BYTES:
            case EXPRESSION:
            case PROPERTY:
            case TYPE:
            case UNDEFINED:
                logger.warn("Unsupported type {} when mapping JSON key {} to DMR attribute {}", type, jsonName,
                        dmrName);
                break;
            default:
                break;
        }
        return node;
    }

    private static ModelNode findAttributeDescription(String name, List<Property> attributeDescriptions) {
        for (Property property : attributeDescriptions) {
            if (name.equals(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }

    private Json() {
    }
}
