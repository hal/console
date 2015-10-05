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
package org.jboss.hal.meta.security;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AbstractMetadataRegistry;
import org.jboss.hal.meta.MetadataCallback;
import org.jboss.hal.meta.StatementContext;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SecurityFramework extends AbstractMetadataRegistry<SecurityContext> {

    private final Map<ResourceAddress, SecurityContext> registry;

    @Inject
    public SecurityFramework(final Environment environment, final StatementContext statementContext) {
        super(statementContext, "security context");
        this.registry = new HashMap<>();
    }

    @Override
    protected SecurityContext lookupAddress(final ResourceAddress address) {
        return registry.get(address);
    }

    @Override
    public void add(final ResourceAddress address, final SecurityContext metadata) {
        registry.put(address, metadata);
    }

    @Override
    protected void addDeferred(final ResourceAddress address, final MetadataCallback<SecurityContext> callback) {
        // TODO
    }
}
