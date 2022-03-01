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
package org.jboss.hal.meta.description;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTE_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

/** Contains the resource and attribute descriptions from the read-resource-description operation. */
/*
 * TODO Refactor methods which use the 'path' parameter (too error prone). Instead use a fluent API:
 *
 * ResourceDescription description = ...;
 *
 * // attributes from the resource description Property attribute = description.atrributes().get("foo"); List<Property>
 * attributes = description.atrributes().group("bar"); List<Property> attributes = description.atrributes().required();
 *
 * // request properties of the ADD operation Property attribute = description.requestProperties().get("foo"); List<Property>
 * attributes = description.requestProperties().group("bar"); List<Property> attributes =
 * description.requestProperties().required();
 */
public class ResourceDescription extends ModelNode {

    public ResourceDescription(ModelNode payload) {
        set(payload);
    }

    /** @return the resource description */
    public String getDescription() {
        return get(DESCRIPTION).asString();
    }

    public List<Property> getAttributes(String path) {
        ModelNode attributes = ModelNodeHelper.failSafeGet(this, path);
        if (attributes.isDefined()) {
            return attributes.asPropertyList();
        }
        return emptyList();
    }

    public List<Property> getAttributes(String path, String group) {
        List<Property> attributes = getAttributes(path);
        return attributes.stream()
                .filter(property -> {
                    ModelNode attributeDescription = property.getValue();
                    return attributeDescription.hasDefined(ATTRIBUTE_GROUP) &&
                            group.equals(attributeDescription.get(ATTRIBUTE_GROUP).asString());
                })
                .collect(toList());
    }

    public List<Property> getRequiredAttributes(String path) {
        return getAttributes(path).stream()
                .filter(property -> {
                    ModelNode attributeDescription = property.getValue();
                    if (attributeDescription.hasDefined(REQUIRED)) {
                        return attributeDescription.get(REQUIRED).asBoolean();
                    } else if (attributeDescription.hasDefined(NILLABLE)) {
                        return !attributeDescription.get(NILLABLE).asBoolean();
                    }
                    return false;
                })
                .collect(toList());
    }

    public List<Property> getOperations() {
        return hasDefined(OPERATIONS) ? get(OPERATIONS).asPropertyList() : emptyList();
    }

    public Property findOperation(String name) {
        if (hasDefined(OPERATIONS)) {
            for (Property property : get(OPERATIONS).asPropertyList()) {
                if (name.equals(property.getName())) {
                    return property;
                }
            }
        }
        return null;
    }

    public Property findAttribute(String path, String name) {
        for (Property property : getAttributes(path)) {
            if (name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    /**
     * Returns the alternatives for the specified attribute.
     *
     * @param path the path to look for the attribute
     * @param name the name of the attribute
     *
     * @return the alternatives for {@code name} or an empty list if {@code name} has no alternatives or if there's no attribute
     *         {@code name}
     */
    public List<String> findAlternatives(String path, String name) {
        Property attribute = findAttribute(path, name);
        if (attribute != null) {
            if (attribute.getValue().hasDefined(ALTERNATIVES)) {
                return attribute.getValue().get(ALTERNATIVES).asList().stream()
                        .map(ModelNode::asString)
                        .collect(toList());
            }
        }
        return emptyList();
    }

    /**
     * Returns the attributes which require the specified attribute.
     *
     * @param path the path to look for the attribute
     * @param name the name of the attribute which is required by the matching attributes
     *
     * @return the attributes which require {@code} or an empty list if no attributes require {@code name} or if there's no
     *         attribute {@code name}
     */
    public List<String> findRequires(String path, String name) {
        return getAttributes(path).stream()
                .filter(attribute -> {
                    if (attribute.getValue().hasDefined(REQUIRES)) {
                        List<String> requires = attribute.getValue().get(REQUIRES).asList().stream()
                                .map(ModelNode::asString)
                                .collect(toList());
                        return requires.contains(name);
                    }
                    return false;
                })
                .map(Property::getName)
                .collect(toList());
    }

    public boolean isDefaultValue(String path, String name, Object value) {
        Property property = findAttribute(path, name);
        if (property != null) {
            ModelNode attribute = property.getValue();
            if (attribute.hasDefined(DEFAULT)) {
                if (value == null) {
                    return true;
                } else {
                    ModelType type = attribute.get(TYPE).asType();
                    if (type.equals(ModelType.INT)) {
                        type = ModelType.LONG;
                    }
                    Object defaultValue = attribute.get(DEFAULT).as(type);
                    return value.equals(defaultValue);
                }
            }
        }
        return false;
    }

    public boolean isDeprecated(String path, String name) {
        Property property = findAttribute(path, name);
        if (property != null) {
            ModelNode attribute = property.getValue();
            return ModelNodeHelper.failSafeBoolean(attribute, DEPRECATED);
        }
        return false;
    }
}
