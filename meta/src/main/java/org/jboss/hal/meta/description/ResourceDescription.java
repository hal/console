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

import java.util.Map;
import java.util.TreeMap;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AttributeCollection;

import static java.util.Collections.emptyList;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTE_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILDREN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STRING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/** Contains the resource and attribute descriptions from the read-resource-description operation. */

public class ResourceDescription extends ModelNode {

    private Map<String, AttributeCollection> map = new TreeMap<>();

    public ResourceDescription(ModelNode payload) {
        set(payload);
    }

    /** @return the resource description */
    public String getDescription() {
        return get(DESCRIPTION).asString();
    }

    public AttributeCollection attributes() {
        return getAttributes(ATTRIBUTES);
    }

    public AttributeCollection requestProperties() {
        return getAttributes(OPERATIONS + "/" + ADD + "/" + REQUEST_PROPERTIES);
    }

    public AttributeCollection operations() {
        return getAttributes(OPERATIONS);
    }

    public AttributeCollection children() {
        return getAttributes(CHILDREN);
    }

    private AttributeCollection getAttributes(String path) {
        ModelNode attributes = ModelNodeHelper.failSafeGet(this, path);
        if (attributes.isDefined()) {
            if (!map.containsKey(path)) {
                for (Property p : attributes.asPropertyList()) {
                    ModelNode parentValue = p.getValue();
                    if (parentValue.hasDefined(TYPE) && parentValue.get(TYPE).asType().equals(ModelType.OBJECT)
                            && !parentValue.get(VALUE_TYPE).asString().equalsIgnoreCase(STRING)) {
                        for (Property nested : parentValue.get(VALUE_TYPE).asPropertyList()) {
                            ModelNode nestedValue = nested.getValue();
                            // inherit from parent
                            if (parentValue.hasDefined(DEPRECATED)) {
                                nestedValue.get(DEPRECATED).set(parentValue.get(DEPRECATED));
                            }
                            if (parentValue.hasDefined(ATTRIBUTE_GROUP)) {
                                nestedValue.get(ATTRIBUTE_GROUP).set(parentValue.get(ATTRIBUTE_GROUP));
                            }
                            nestedValue.get(STORAGE).set(parentValue.get(STORAGE));
                            nestedValue.get(ACCESS_TYPE).set(parentValue.get(ACCESS_TYPE));
                            // "required"/"nillable" has to depend on parent value
                            boolean combined = parentValue.get(NILLABLE).asBoolean()
                                    || nestedValue.get(NILLABLE).asBoolean();
                            nestedValue.get(NILLABLE).set(combined);
                            combined = parentValue.get(REQUIRED).asBoolean()
                                    && nestedValue.get(REQUIRED).asBoolean();
                            nestedValue.get(REQUIRED).set(combined);
                            attributes.get(p.getName() + "." + nested.getName()).set(nestedValue);
                        }
                    }
                }
                map.put(path, new AttributeCollection(attributes.asPropertyList()));
            }
        } else {
            map.put(path, new AttributeCollection(emptyList()));
        }
        return map.get(path);
    }
}
