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
package org.jboss.hal.meta.processing;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityFramework;

import java.util.Set;

import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;

/**
 * @author Harald Pehl
 */
class Lookup {

    private final ResourceDescriptions descriptionRegistry;
    private final SecurityFramework securityFramework;

    Lookup(ResourceDescriptions descriptionRegistry, SecurityFramework securityFramework) {
        this.descriptionRegistry = descriptionRegistry;
        this.securityFramework = securityFramework;
    }

    public LookupResult check(Set<AddressTemplate> templates, boolean recursive) {
        LookupResult lookupResult = new LookupResult(templates, recursive);
        for (AddressTemplate template : lookupResult.templates()) {
            if (descriptionRegistry.contains(template)) {
                lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
            }
            if (securityFramework.contains(template)) {
                lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
            }
        }
        return lookupResult;
    }
}
