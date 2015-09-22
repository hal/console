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
package org.jboss.hal.core.meta;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import org.jboss.hal.dmr.model.AddressTemplate;
import org.jboss.hal.dmr.model.StatementContext;
import org.jboss.hal.security.SecurityContext;
import org.jboss.hal.security.SecurityFramework;
import org.jboss.hal.security.UnresolvedSecurityContext;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @author Harald Pehl
 */
public class SecurityFrameworkImpl implements SecurityFramework {

    @FunctionalInterface
    interface KeyProvider {

        LinkedHashSet<String> keys(AddressTemplate template);
    }

    private final StatementContext statementContext;
    private final Map<String, SecurityContext> contextMap;
    private final KeyProvider asIs;

    @Inject
    public SecurityFrameworkImpl(StatementContext statementContext) {
        this.statementContext = statementContext;
        this.contextMap = new HashMap<>();
        this.asIs = template -> Sets.newLinkedHashSet(singletonList(template.getTemplate()));
    }

    @Override
    public SecurityContext lookup(final AddressTemplate address) throws UnresolvedSecurityContext {
        SecurityContext securityContext = internalLookup(address);
        if (securityContext == null) {
            throw new UnresolvedSecurityContext(address);
        }
        return securityContext;
    }

    @Override
    public SecurityContext lookup(final AddressTemplate address, final Supplier<SecurityContext> provider) {
        SecurityContext securityContext = internalLookup(address);
        return securityContext != null ? securityContext : provider.get();
    }

    private SecurityContext internalLookup(AddressTemplate address) {
        KeyProvider keyProvider = chooseResolver(address);
        for (String key : keyProvider.keys(address)) {
            SecurityContext securityContext = contextMap.get(key);
            if (securityContext != null) {
                return securityContext;
            }
        }
        return null;
    }

    void assignContext(String key, SecurityContext securityContext) {
        contextMap.put(key, securityContext);
    }

    private KeyProvider chooseResolver(final AddressTemplate address) {
        KeyProvider keyProvider = asIs;
        if (!address.isEmpty()) {
            // TODO Choose different key providers depending on the address template
            AddressTemplate subTemplate = address.subTemplate(0, 1);
            if (AddressTemplate.of("{selected.group}").equals(subTemplate)) {

            } else if (AddressTemplate.of("{selected.host}").equals(subTemplate)) {
                if (address.size() > 1 && AddressTemplate.of("{selected.host}").equals(address.subTemplate(1, 2))) {

                } else {

                }
            }
        }
        return keyProvider;
    }
}
