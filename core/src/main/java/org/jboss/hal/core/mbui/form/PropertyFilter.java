/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.mbui.form;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSortedSet;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class PropertyFilter {

    private final ModelNodeForm.Builder builder;
    private final Iterable<Property> input;

    PropertyFilter(final ModelNodeForm.Builder builder) {
        this.builder = builder;
        this.input = builder.createResource ?
                builder.resourceDescription.get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES).asPropertyList() :
                builder.resourceDescription.get(ATTRIBUTES).asPropertyList();

    }

    ImmutableSortedSet<Property> filter() {
        Predicate<Property> filter;

        if (builder.createResource) {
            filter = property -> isRequired(property.getValue());
            if (!builder.includes.isEmpty()) {
                filter = Predicates.or(filter, property -> builder.includes.contains(property.getName()));
            }

        } else {
            if (builder.includes.isEmpty() && builder.excludes.isEmpty()) {
                filter = Predicates.alwaysTrue();
            } else if (!builder.excludes.isEmpty()) {
                filter = property -> !builder.excludes.contains(property.getName());
            } else {
                filter = property -> builder.includes.contains(property.getName());
            }
            if (builder.includeRuntime) {
                filter = Predicates.and(filter,
                        property -> "runtime".equals(property.getValue().get(STORAGE).asString()));
            }
        }

        return FluentIterable.from(input)
                .filter(filter)
                .toSortedSet((p1, p2) -> p1.getName().compareTo(p2.getName())); // removes duplicates
    }

    private boolean isRequired(ModelNode modelNode) {
        //noinspection SimplifiableConditionalExpression
        boolean nillable = modelNode.hasDefined(NILLABLE) ? modelNode.get(NILLABLE).asBoolean() : true;
        boolean alternatives = modelNode.hasDefined("alternatives") &&
                !modelNode.get("alternatives").asList().isEmpty();
        return !nillable && !alternatives;
    }
}
