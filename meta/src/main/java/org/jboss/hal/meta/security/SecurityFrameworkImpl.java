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
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.dmr.ResourceAddress;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public class SecurityFrameworkImpl implements SecurityFramework {

    private class SecurityStatementContext extends FilteringStatementContext {

        public SecurityStatementContext(final StatementContext delegate) {
            super(delegate, new Filter() {
                @Override
                public String filter(final String key) {
                    return null;
                }

                @Override
                public String[] filterTuple(final String key) {
                    return new String[0];
                }
            });
        }
    }


    private final StatementContext statementContext;
    private final Map<ResourceAddress, SecurityContext> contextMap;

    @Inject
    public SecurityFrameworkImpl(final Environment environment, final StatementContext statementContext) {
        this.statementContext = new SecurityStatementContext(statementContext);
        this.contextMap = new HashMap<>();
    }

    @Override
    public SecurityContext lookup(final AddressTemplate template) throws UnresolvedSecurityContext {
        SecurityContext securityContext = internalLookup(template);
        if (securityContext == null) {
            throw new UnresolvedSecurityContext(template);
        }
        return securityContext;
    }

    @Override
    public void lookupDeferred(final AddressTemplate template, final SecurityContextCallback callback) {
        SecurityContext securityContext = internalLookup(template);
        if (securityContext == null) {
            // TODO create and register the context asynchronously
        } else {
            callback.onContext(securityContext);
        }
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
