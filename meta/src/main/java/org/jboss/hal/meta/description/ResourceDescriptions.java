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

import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AbstractMetadataRegistry;
import org.jboss.hal.meta.MetadataCallback;
import org.jboss.hal.meta.StatementContext;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ResourceDescriptions extends AbstractMetadataRegistry<ResourceDescription> {

    // TODO Replace map with local storage (constrained by language and management model version)
    private final Map<ResourceAddress, ResourceDescription> registry;

    @Inject
    public ResourceDescriptions(final StatementContext statementContext) {
        super(statementContext, "resource description");
        this.registry = new HashMap<>();
    }

    @Override
    protected ResourceDescription lookupAddress(final ResourceAddress address) {
        return registry.get(address);
    }

    @Override
    public void add(final ResourceAddress address, final ResourceDescription metadata) {
        // TODO replace concrete addresses with wildcards
        registry.put(address, metadata);
    }

    @Override
    protected void addDeferred(final ResourceAddress address, final MetadataCallback<ResourceDescription> callback) {
        // TODO
    }
}
