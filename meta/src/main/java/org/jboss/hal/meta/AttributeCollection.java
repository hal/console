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
package org.jboss.hal.meta;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTE_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

/** Wrapper around a set of attribute descriptions to avoid direct manipulation of the underlying ModelNode */
public class AttributeCollection implements Collection<Property> {
    private List<Property> list;

    public AttributeCollection(List<Property> list) {
        this.list = list;
    }

    public ModelNode get(String name) {
        for (Property p : this) {
            if (p.getName().equals(name)) {
                return p.getValue();
            }
        }
        return new ModelNode();
    }

    public Property property(String name) {
        return new Property(name, get(name));
    }

    public String description(String name) {
        return get(name).get(DESCRIPTION).asString();
    }

    public String capabilityReference(String name) {
        return get(name).get(CAPABILITY_REFERENCE).asString();
    }

    public List<Property> group(String group) {
        return stream()
                .filter(property -> {
                    ModelNode attributeDescription = property.getValue();
                    return attributeDescription.hasDefined(ATTRIBUTE_GROUP) &&
                            group.equals(attributeDescription.get(ATTRIBUTE_GROUP).asString());
                })
                .collect(toList());
    }

    public List<Property> required() {
        return stream()
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

    /**
     * Returns the alternatives for the specified attribute.
     *
     * @param name the name of the attribute
     *
     * @return the alternatives for {@code name} or an empty list if {@code name} has no alternatives or if there's no attribute
     *         {@code name}
     */
    public List<String> alternatives(String name) {
        ModelNode attribute = get(name);
        if (attribute.hasDefined(ALTERNATIVES)) {
            return attribute.get(ALTERNATIVES).asList().stream()
                    .map(ModelNode::asString)
                    .collect(toList());
        }
        return emptyList();
    }

    /**
     * Returns the attributes which require the specified attribute.
     *
     * @param name the name of the attribute which is required by the matching attributes
     *
     * @return the attributes which require {@code} or an empty list if no attributes require {@code name} or if there's no
     *         attribute {@code name}
     */
    public List<String> requiredBy(String name) {
        return stream()
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

    public boolean isDefaultValue(String name, Object value) {
        ModelNode attribute = get(name);
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
        return false;
    }

    public boolean isDeprecated(String name) {
        return get(name).has(DEPRECATED);
    }

    // ------------ collection overrides

    @Override
    public boolean add(Property property) {
        return list.add(property);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<Property> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return list.toArray(ts);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return list.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends Property> collection) {
        return list.addAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return list.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return list.retainAll(collection);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public void forEach(Consumer<? super Property> action) {
        list.forEach(action);
    }

    @Override
    public Stream<Property> stream() {
        return list.stream();
    }
}
