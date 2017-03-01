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
package org.jboss.hal.meta.description;

import java.util.Collections;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Represents the result of a read-resource-description operation for one specific resource.
 *
 * @author Harald Pehl
 */
public class ResourceDescription extends ModelNode {

    public ResourceDescription(ModelNode payload) {
        set(payload);
    }

    public String getDescription() {
        return get(DESCRIPTION).asString();
    }

    public List<Property> getAttributes(final String path) {
        ModelNode attributes = ModelNodeHelper.failSafeGet(this, path);
        if (attributes.isDefined()) {
            return attributes.asPropertyList();
        }
        return Collections.emptyList();
    }

    public List<Property> getAttributes(final String path, final String group) {
        List<Property> attributes = getAttributes(path);
        return attributes.stream()
                .filter(property -> {
                    ModelNode attributeDescription = property.getValue();
                    return attributeDescription.hasDefined(ATTRIBUTE_GROUP) &&
                            group.equals(attributeDescription.get(ATTRIBUTE_GROUP).asString());
                })
                .collect(toList());
    }

    public List<Property> getRequiredAttributes(final String path) {
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
        return hasDefined(OPERATIONS) ? get(OPERATIONS).asPropertyList() : Collections.emptyList();
    }

    public Property findAttribute(final String path, final String name) {
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
     * @return the alternatives for {@code name} or an empty list if {@code name} has no alternatives or if there's no
     * attribute {@code name}
     */
    public List<String> findAlternatives(final String path, final String name) {
        Property attribute = findAttribute(path, name);
        if (attribute != null) {
            if (attribute.getValue().hasDefined(ALTERNATIVES)) {
                return attribute.getValue().get(ALTERNATIVES).asList().stream()
                        .map(ModelNode::asString)
                        .collect(toList());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the attributes which require the specified attribute.
     *
     * @param path the path to look for the attribute
     * @param name the name of the attribute which is required by the matching attributes
     *
     * @return the attributes which require {@code} or an empty list if no attributes require {@code name} or if there's
     * no attribute {@code name}
     */
    public List<String> findRequires(final String path, final String name) {
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

    public boolean isDefaultValue(final String path, final String name, final Object value) {
        if (value != null) {
            Property attribute = findAttribute(path, name);
            if (attribute != null) {
                if (attribute.getValue().hasDefined(DEFAULT)) {
                    ModelType type = attribute.getValue().get(TYPE).asType();
                    Object defaultValue = attribute.getValue().get(DEFAULT).as(type);
                    return value.equals(defaultValue);
                }
            }
        }
        return false;
    }

    public boolean isDeprecated() {
        return hasDefined(DEPRECATED);
    }
}
