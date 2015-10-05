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
package org.jboss.hal.meta;

import org.jboss.hal.dmr.model.ResourceAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Pehl
 */
public abstract class AbstractMetadataRegistry<T> implements MetadataRegistry<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMetadataRegistry.class);

    private final StatementContext statementContext;
    private final String type;

    protected AbstractMetadataRegistry(final StatementContext statementContext, final String type) {
        this.statementContext = statementContext;
        this.type = type;
    }

    @Override
    public T lookup(AddressTemplate template) throws MissingMetadataException {
        ResourceAddress address = resolveTemplate(template);
        T metadata = lookupAddress(address);
        if (metadata == null) {
            throw new MissingMetadataException(type, template);
        }
        return metadata;
    }

    @Override
    public void lookupDeferred(final AddressTemplate template, final MetadataCallback<T> callback) {
        ResourceAddress address = resolveTemplate(template);
        T metadata = lookupAddress(address);
        if (metadata == null) {
            addDeferred(address, callback);
        } else {
            callback.onContext(metadata);
        }
    }

    @Override
    public boolean contains(AddressTemplate template) {
        ResourceAddress address = resolveTemplate(template);
        return lookupAddress(address) != null;
    }

    private ResourceAddress resolveTemplate(AddressTemplate template) {
        return template.resolve(statementContext);
    }

    protected abstract T lookupAddress(ResourceAddress address);

    protected abstract void addDeferred(ResourceAddress address, final MetadataCallback<T> callback);
}
