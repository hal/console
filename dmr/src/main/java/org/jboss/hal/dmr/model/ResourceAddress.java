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
package org.jboss.hal.dmr.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

/**
 * Represents a fully qualified DMR address ready to be put into a DMR operation.
 *
 * @author Harald Pehl
 */
public class ResourceAddress extends ModelNode {

    public static final ResourceAddress ROOT = new ResourceAddress();

    public ResourceAddress() {
        setEmptyList();
    }

    public ResourceAddress(ModelNode address) {
        set(address);
    }

    public ResourceAddress add(final String propertyName, final String propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    public ResourceAddress add(ResourceAddress address) {
        if (address != null) {
            for (Property property : address.asPropertyList()) {
                add(property.getName(), property.getValue().asString());
            }
        }
        return this;
    }

    public String lastName() {
        List<Property> properties = asPropertyList();
        if (!properties.isEmpty()) {
            return properties.get(properties.size() - 1).getName();
        }
        return null;
    }

    public String lastValue() {
        List<Property> properties = asPropertyList();
        if (!properties.isEmpty()) {
            return properties.get(properties.size() - 1).getValue().asString();
        }
        return null;
    }

    public ResourceAddress getParent() {
        if (this == ROOT || asList().isEmpty()) {
            return this;
        }
        List<ModelNode> parent = new ArrayList<>(asList());
        parent.remove(parent.size() - 1);
        return new ResourceAddress(new ModelNode().set(parent));
    }

    public int size() {
        return isDefined() ? asList().size() : 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isDefined()) {
            builder.append("/");
            for (Iterator<Property> iterator = asPropertyList().iterator(); iterator.hasNext(); ) {
                Property segment = iterator.next();
                builder.append(segment.getName()).append("=").append(segment.getValue().asString());
                if (iterator.hasNext()) {
                    builder.append("/");
                }
            }
        }
        return builder.toString();
    }
}
