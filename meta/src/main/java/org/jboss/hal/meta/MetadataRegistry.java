/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta;

import javax.inject.Inject;

import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContextRegistry;

/** Registry for existing resource {@link Metadata}. */
public class MetadataRegistry implements Registry<Metadata> {

    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextRegistry securityContextRegistry;
    private final Capabilities capabilities;

    @Inject
    public MetadataRegistry(ResourceDescriptionRegistry resourceDescriptionRegistry,
            SecurityContextRegistry securityContextRegistry,
            Capabilities capabilities) {
        this.securityContextRegistry = securityContextRegistry;
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.capabilities = capabilities;
    }

    @Override
    public Metadata lookup(AddressTemplate template) throws MissingMetadataException {
        ResourceDescription resourceDescription = resourceDescriptionRegistry.lookup(template);
        return new Metadata(template, () -> securityContextRegistry.lookup(template), resourceDescription,
                capabilities);
    }

    @Override
    public boolean contains(AddressTemplate template) {
        return securityContextRegistry.contains(template) &&
                resourceDescriptionRegistry.contains(template);
    }
}
