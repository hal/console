/*
 *  Copyright 2023 Red Hat
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
package org.jboss.hal.core.mbui.form;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;

public class FormHelper {

    public static List<Property> getResettableProperties(Set<String> attributes, Metadata metadata) {
        ResourceDescription description = metadata.getDescription();

        // collect all attributes from the 'requires' list of this attribute
        TreeSet<String> requires = new TreeSet<>();
        ModelNode attributesDescription = description.get(ATTRIBUTES);
        attributes.forEach(attribute -> {
            ModelNode attributeDescription = attributesDescription.get(attribute);
            if (attributeDescription != null && attributeDescription.hasDefined(REQUIRES)) {
                failSafeList(attributeDescription, REQUIRES).forEach(node -> requires.add(node.asString()));
            }
        });

        List<String> deprecated = attributes.stream().filter(attribute -> attributesDescription.get(attribute).has(DEPRECATED))
                .collect(Collectors.toList());

        return attributes.stream()
                .map(attribute -> description.findAttribute(ATTRIBUTES, attribute))
                .filter(prop -> Objects.nonNull(prop)
                        && !requires.contains(prop.getName())
                        && isNillable(prop.getValue())
                        && !isReadonly(prop.getValue())
                        && !hasAlternatives(prop.getValue(), attributes, deprecated))
                .collect(Collectors.toList());
    }

    private static boolean isNillable(ModelNode attr) {
        return attr.hasDefined(NILLABLE) && attr.get(NILLABLE).asBoolean();
    }

    private static boolean isReadonly(ModelNode attr) {
        return attr.hasDefined(ACCESS_TYPE) && READ_ONLY.equals(attr.get(ACCESS_TYPE).asString());
    }

    private static boolean hasAlternatives(ModelNode attr, Set<String> attributes, List<String> deprecated) {
        if (attr.hasDefined(ALTERNATIVES) && !attr.get(ALTERNATIVES).asList().isEmpty()) {
            return failSafeList(attr, ALTERNATIVES).stream()
                    .map(ModelNode::asString)
                    // ignore deprecated alternatives
                    .filter(name -> attributes.contains(name) && !deprecated.contains(name))
                    .count() > 0;
        }
        return false;
    }
}
