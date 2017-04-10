/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta.processing;

import java.util.Set;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContextRegistry;

import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;

/**
 * @author Harald Pehl
 */
class Lookup {

    private final SecurityContextRegistry securityContextRegistry;
    private final ResourceDescriptionRegistry descriptionRegistry;

    Lookup(SecurityContextRegistry securityContextRegistry, ResourceDescriptionRegistry descriptionRegistry) {
        this.securityContextRegistry = securityContextRegistry;
        this.descriptionRegistry = descriptionRegistry;
    }

    public LookupResult check(Set<AddressTemplate> templates, boolean recursive) {
        LookupResult lookupResult = new LookupResult(templates, recursive);
        for (AddressTemplate template : lookupResult.templates()) {
            if (descriptionRegistry.contains(template)) {
                lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
            }
            if (securityContextRegistry.contains(template)) {
                lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
            }
        }
        return lookupResult;
    }
}
