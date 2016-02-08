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
package org.jboss.hal.dmr;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.resources.Names.NAME_KEY;

/**
 * Static helper methods for dealing with {@link ModelNode}s.
 *
 * @author Harald Pehl
 */
public final class ModelNodeHelper {

    private ModelNodeHelper() {}

    /**
     * Tries to get a deeply nested model node from the specified model node. Nested paths must be separated with ".".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "."
     *
     * @return The nested node or an empty / undefined model node
     */
    public static ModelNode failSafeGet(final ModelNode modelNode, final String path) {
        ModelNode undefined = new ModelNode();

        if (Strings.emptyToNull(path) != null) {
            Iterable<String> keys = Splitter.on('.').omitEmptyStrings().trimResults().split(path);
            if (!Iterables.isEmpty(keys)) {
                ModelNode context = modelNode;
                for (String key : keys) {
                    if (context.hasDefined(key)) {
                        context = context.get(key);
                    } else {
                        context = undefined;
                        break;
                    }
                }
                return context;
            }
        }

        return undefined;
    }

    public static <T> T getOrDefault(final ModelNode modelNode, Provider<T> provider, T defaultValue) {
        T result = defaultValue;
        if (modelNode != null) {
            try {
                result = provider.get();
            } catch (Throwable t) {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
     * Turns a list of properties into a list of model nodes which contains a {@link
     * org.jboss.hal.resources.Names#NAME_KEY} key with the properties name.
     */
    public static List<ModelNode> asNodesWithNames(List<Property> properties) {
        List<ModelNode> nodes = new ArrayList<>(properties.size());
        for (Property property : properties) {
            property.getValue().get(NAME_KEY).set(property.getName());
            nodes.add(property.getValue());
        }
        return nodes;
    }
}
