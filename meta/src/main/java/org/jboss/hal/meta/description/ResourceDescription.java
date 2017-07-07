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

import java.util.List;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.spi.EsReturn;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Contains the resource and attribute descriptions from the read-resource-description operation.
 *
 * @author Harald Pehl
 */
@JsType(namespace = "hal.meta")
public class ResourceDescription extends ModelNode {

    @JsIgnore
    public ResourceDescription(final ModelNode payload) {
        set(payload);
    }

    /**
     * @return the resource description
     */
    @JsProperty
    public String getDescription() {
        return get(DESCRIPTION).asString();
    }

    @JsIgnore
    public List<Property> getAttributes(final String path) {
        ModelNode attributes = ModelNodeHelper.failSafeGet(this, path);
        if (attributes.isDefined()) {
            return attributes.asPropertyList();
        }
        return emptyList();
    }

    @JsIgnore
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

    @JsIgnore
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

    @JsIgnore
    public List<Property> getOperations() {
        return hasDefined(OPERATIONS) ? get(OPERATIONS).asPropertyList() : emptyList();
    }

    @JsIgnore
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
    @JsIgnore
    public List<String> findAlternatives(final String path, final String name) {
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
     * @return the attributes which require {@code} or an empty list if no attributes require {@code name} or if there's
     * no attribute {@code name}
     */
    @JsIgnore
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

    @JsIgnore
    public boolean isDefaultValue(final String path, final String name, final Object value) {
        Property property = findAttribute(path, name);
        if (property != null) {
            ModelNode attribute = property.getValue();
            if (attribute.hasDefined(DEFAULT)) {
                if (value == null) {
                    return true;
                } else {
                    ModelType type = attribute.get(TYPE).asType();
                    Object defaultValue = attribute.get(DEFAULT).as(type);
                    return value.equals(defaultValue);
                }
            }
        }
        return false;
    }


    // ------------------------------------------------------ JS methods

    /**
     * @return the attribute descriptions
     */
    @JsMethod(name = "getAttributes")
    @EsReturn("Property[]")
    public Property[] jsGetAttributes() {
        List<Property> attributes = getAttributes(ATTRIBUTES);
            return attributes.toArray(new Property[attributes.size()]);
    }

    /**
     * @return the request properties of the add operation
     */
    @JsMethod(name = "getRequestProperties")
    @EsReturn("Property[]")
    public Property[] jsGetRequestProperties() {
        List<Property> attributes = getAttributes(OPERATIONS + "/" + ADD + "/" + REQUEST_PROPERTIES);
        return attributes.toArray(new Property[attributes.size()]);
    }

    /**
     * @return the operation descriptions
     */
    @JsProperty(name = "operations")
    @EsReturn("Property[]")
    public Property[] jsOperations() {
        List<Property> operations = getOperations();
        return operations.toArray(new Property[operations.size()]);
    }
}
