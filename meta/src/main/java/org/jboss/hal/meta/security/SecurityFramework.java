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
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MetadataCallback;
import org.jboss.hal.meta.MissingMetadataException;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.dmr.model.ResourceAddress;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SecurityFramework {

    private final Environment environment;
    private final StatementContext statementContext;
    private final Map<ResourceAddress, SecurityContext> contextMap;

    @Inject
    public SecurityFramework(final Environment environment, final StatementContext statementContext) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.contextMap = new HashMap<>();
    }

    public SecurityContext lookup(final AddressTemplate template) throws MissingMetadataException {
        SecurityContext securityContext = internalLookup(template);
        if (securityContext == null) {
            throw new MissingMetadataException("SecurityFramework", template);
        }
        return securityContext;
    }

    public void lookupDeferred(final AddressTemplate template, final MetadataCallback<SecurityContext> callback) {
        SecurityContext securityContext = internalLookup(template);
        if (securityContext == null) {
            // TODO create and register the context asynchronously
        } else {
            callback.onContext(securityContext);
        }
    }

    public boolean contains(AddressTemplate template) {
        return internalLookup(template) != null;
    }

    void assignContext(AddressTemplate template, SecurityContext securityContext) {
        ResourceAddress address = resolveTemplate(template);
        contextMap.put(address, securityContext);
    }

    private SecurityContext internalLookup(AddressTemplate template) {
        ResourceAddress address = resolveTemplate(template);
        return contextMap.get(address);
    }

    private ResourceAddress resolveTemplate(AddressTemplate template) {
        return template.resolve(statementContext);
    }
}
