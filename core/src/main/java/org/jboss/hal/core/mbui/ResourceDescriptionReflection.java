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
import com.google.common.collect.Ordering;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemFactory;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final FormItemFactory defaultFormItemFactory;

    private int numRequired;
    private int numReadOnly;
    private int numWriteable;
    private Map<String, FormItem> formItems;
    private Map<String, String> helpTexts;
    private Map<String, ModelNode> defaults;

    ResourceDescriptionReflection(final ResourceDescription resourceDescription,
            final Set<String> includes, final Set<String> excludes, final boolean includeRuntime,
            final Map<String, FormItemFactory> factories) {

        this.defaultFormItemFactory = new DefaultFormItemFactory();

        this.numReadOnly = 0;
        this.numReadOnly = 0;
        this.numWriteable = 0;
        this.formItems = new LinkedHashMap<>();
        this.helpTexts = new LinkedHashMap<>();
        this.defaults = new HashMap<>();

        List<Property> attributes = filter(resourceDescription, includes, excludes, includeRuntime);
        analyze(attributes, factories);
    }

    private List<Property> filter(final ResourceDescription resourceDescription, final Set<String> includes,
            final Set<String> excludes, final boolean includeRuntime) {

        Predicate<Property> includeExclude = includes.isEmpty() && excludes.isEmpty() ?
                Predicates.alwaysTrue() :
                (property -> includes.contains(property.getName()) && !excludes.contains(property.getName()));

        Predicate<Property> runtime = property -> "runtime"
                .equals(property.getValue().get(ModelDescriptionConstants.STORAGE).asString());

        Predicate<Property> combined = includeRuntime ? Predicates.and(includeExclude, runtime) : includeExclude;
        Collection<Property> filtered = Collections2.filter(resourceDescription.getAttributes(), combined);

        Ordering<Property> byName = new Ordering<Property>() {
            @Override
            public int compare(final Property p1, final Property p2) {
                return p1.getName().compareTo(p2.getName());
            }
        };
        return byName.immutableSortedCopy(filtered);
    }

    private void analyze(final List<Property> attributes, final Map<String, FormItemFactory> factories) {
        for (Property property : attributes) {
            String name = property.getName();
            ModelNode attribute = property.getValue();

            FormItem formItem;
            if (factories.containsKey(name)) {
                formItem = factories.get(name).createFrom(attribute);
            } else {
                formItem = defaultFormItemFactory.createFrom(attribute);
            }
            formItems.put(name, formItem);


        }
    }

    Map<String, ModelNode> getDefaults() {
        return defaults;
    }

    Map<String, FormItem> getFormItems() {
        return formItems;
    }

    Map<String, String> getHelpTexts() {
        return helpTexts;
    }
}
