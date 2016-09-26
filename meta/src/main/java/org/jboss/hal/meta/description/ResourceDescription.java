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
    
    public boolean isDeprecated() {
        return hasDefined(DEPRECATED);
    }
    
    public String getDeprecatedMessage() {
        String message = "";
        if (hasDefined(DEPRECATED)) {
            message = get(DEPRECATED).get(REASON).asString();
        }
        return message;
    }
}
