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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Represents the result of a read-resource-description operation for one specific resource.
 *
 * @author Harald Pehl
 */
public class ResourceDescription extends ModelNode {

    public ResourceDescription() {
        super();
    }

    public ResourceDescription(ModelNode payload) {
        set(payload);
    }

    public String getDescription() {
        return get(DESCRIPTION).asString();
    }

    public boolean hasAttributes() {
        return hasDefined(ATTRIBUTES) && !get(ATTRIBUTES).asList().isEmpty();
    }

    public List<Property> getAttributes() {
        return hasAttributes() ?  get(ATTRIBUTES).asPropertyList() : Collections.emptyList();
    }

    public List<Property> getRequiredAttributes() {
        if (hasAttributes()) {
            Iterable<Property> required = Iterables.filter(getAttributes(),
                    requestProperty -> requestProperty.getValue().hasDefined(NILLABLE) &&
                            !requestProperty.getValue().get(NILLABLE).asBoolean());
            return Lists.newArrayList(required);

        } else {
            return Collections.emptyList();
        }
    }

    public List<Property> getRequestProperties() {
        String path = OPERATIONS + "." + ADD + "." + REQUEST_PROPERTIES;
        ModelNode requestProperties = ModelNodeHelper.failSafeGet(this, path);
        if (requestProperties.isDefined()) {
            return requestProperties.asPropertyList();
        } else {
            return Collections.emptyList();
        }
    }

    public List<Property> getRequiredRequestProperties() {
        String path = OPERATIONS + "." + ADD + "." + REQUEST_PROPERTIES;
        ModelNode requestProperties = ModelNodeHelper.failSafeGet(this, path);

        if (requestProperties.isDefined()) {
            Iterable<Property> required = Iterables.filter(getRequestProperties(),
                    requestProperty -> requestProperty.getValue().hasDefined(REQUIRED) &&
                            requestProperty.getValue().get(REQUIRED).asBoolean());
            return Lists.newArrayList(required);

        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasOperations() {
        return hasDefined(OPERATIONS) && !get(OPERATIONS).asList().isEmpty();
    }

    public List<Property> getOperations() {
        return hasOperations() ? get(OPERATIONS).asPropertyList() : Collections.emptyList();
    }

    public ModelNode findAttribute(String name) {
        List<Property> properties = hasAttributes() ? getAttributes() : getRequestProperties();
        for (Property property : properties) {
            if (name.equals(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }
}
