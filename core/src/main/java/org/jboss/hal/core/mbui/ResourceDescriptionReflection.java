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
package org.jboss.hal.core.mbui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Harald Pehl
 */
class ResourceDescriptionReflection {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDescriptionReflection.class);

    private final ResourceDescription resourceDescription;
    private final Set<String> includes;
    private final Set<String> excludes;
    private final Map<String, ModelNodeFormItemFactory> factories;

    private int numRequired;
    private int numReadOnly;
    private int numWriteable;
    private List<FormItem> formItems;
    private Map<String, String> helpTexts;
    private Map<String, ModelNode> defaults;

    /**
     * Creates form items from the specified resource description. If {@code attributes} is empty all attributes
     * are processed, otherwise only the given attributes.
     */
    ResourceDescriptionReflection(final ResourceDescription resourceDescription,
            final Set<String> includes, final Set<String> excludes,
            final Map<String, ModelNodeFormItemFactory> factories) {
        this.resourceDescription = resourceDescription;
        this.includes = includes;
        this.excludes = excludes;
        this.factories = factories;

        this.numReadOnly = 0;
        this.numReadOnly = 0;
        this.numWriteable = 0;
        this.formItems = new ArrayList<>();
        this.helpTexts = new LinkedHashMap<>();
        this.defaults = new HashMap<>();

        analyze();
    }

    private void analyze() {
        Predicate<Property> predicate = includes.isEmpty() && excludes.isEmpty() ?
                Predicates.alwaysTrue() :
                (property -> includes.contains(property.getName()) && !excludes.contains(property.getName()));
        Collection<Property> filtered = Collections2.filter(resourceDescription.getAttributes(), predicate);

        Ordering<Property> byName = new Ordering<Property>() {
            @Override
            public int compare(final Property p1, final Property p2) {
                return p1.getName().compareTo(p2.getName());
            }
        };
        ImmutableList<Property> sorted = byName.immutableSortedCopy(filtered);

        for (Property property : sorted) {
            ModelNode attribute = property.getValue();
        }
    }

    Map<String, ModelNode> getDefaults() {
        return defaults;
    }

    List<FormItem> getFormItems() {
        return formItems;
    }

    Map<String, String> getHelpTexts() {
        return helpTexts;
    }
}
