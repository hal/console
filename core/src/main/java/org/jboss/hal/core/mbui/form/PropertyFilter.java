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
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;

/**
 * @author Harald Pehl
 */
class PropertyFilter implements Predicate<Property> {

    private final ModelNodeForm.Builder builder;

    PropertyFilter(final ModelNodeForm.Builder builder) {
        this.builder = builder;
    }

    @Override
    public boolean apply(final Property property) {
        Predicate<Property> filter;

        if (builder.createResource) {
            // include *all* required properties plus the ones in builder.includes
            filter = p -> p.getValue().hasDefined(REQUIRED) && p.getValue().get(REQUIRED).asBoolean();
            if (!builder.includes.isEmpty()) {
                filter = Predicates.or(filter, p -> builder.includes.contains(p.getName()));
            }

        } else {
            if (builder.includes.isEmpty() && builder.excludes.isEmpty()) {
                filter = Predicates.alwaysTrue();
            } else if (!builder.excludes.isEmpty()) {
                filter = p -> !builder.excludes.contains(p.getName());
            } else {
                filter = p -> builder.includes.contains(p.getName());
            }
            if (builder.includeRuntime) {
                filter = Predicates.and(filter, p -> "runtime".equals(p.getValue().get(STORAGE).asString()));
            }
        }

        return filter.apply(property);
    }
}
