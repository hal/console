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
package org.jboss.hal.meta.description;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MetadataCallback;
import org.jboss.hal.meta.MissingMetadataException;

import java.util.HashMap;
import java.util.Map;

public class ResourceDescriptions {

    // TODO Replace map with local storage (constrained by language and management model version)
    private final Map<AddressTemplate, ResourceDescription> registry;

    public ResourceDescriptions() {
        registry = new HashMap<>();
    }

    public void add(AddressTemplate addressTemplate, ResourceDescription description) {
        registry.put(addressTemplate, description);
    }

    public ResourceDescription lookup(AddressTemplate template) throws MissingMetadataException {
        ResourceDescription resourceDescription = registry.get(template);
        if (resourceDescription == null) {
            throw new MissingMetadataException("ResourceDescription", template);
        }
        return resourceDescription;
    }

    public void lookupDeferred(final AddressTemplate template, final MetadataCallback<ResourceDescription> callback) {
        ResourceDescription resourceDescription = registry.get(template);
        if (resourceDescription == null) {
            // TODO create and register the context asynchronously
        } else {
            callback.onContext(resourceDescription);
        }
    }

    public boolean contains(AddressTemplate addressTemplate) {
        return registry.containsKey(addressTemplate);
    }
}
