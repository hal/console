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
package org.jboss.hal.meta;

import javax.inject.Inject;

import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextRegistry;

/**
 * Registry for {@link Metadata} which combines the information from {@link ResourceDescriptionRegistry},
 * {@link SecurityContextRegistry} and {@link org.jboss.hal.meta.capabilitiy.Capabilities}.
 * <p>
 * Does not hold own state, but simply returns metadata which is built by using the registries from above.
 *
 * @author Harald Pehl
 */
public class MetadataRegistry implements Registry<Metadata> {

    private final SecurityContextRegistry securityContextRegistry;
    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final Capabilities capabilities;

    @Inject
    public MetadataRegistry(final SecurityContextRegistry securityContextRegistry,
            final ResourceDescriptionRegistry resourceDescriptionRegistry,
            final Capabilities capabilities) {
        this.securityContextRegistry = securityContextRegistry;
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.capabilities = capabilities;
    }

    @Override
    public Metadata lookup(final AddressTemplate template) throws MissingMetadataException {
        SecurityContext securityContext = securityContextRegistry.lookup(template);
        ResourceDescription resourceDescription = resourceDescriptionRegistry.lookup(template);
        Metadata metadata = new Metadata(template, securityContext, resourceDescription, capabilities);
        metadata.injectSecurityContextRegistry(securityContextRegistry);
        return metadata;
    }

    @Override
    public boolean contains(final AddressTemplate template) {
        return securityContextRegistry.contains(template) && resourceDescriptionRegistry.contains(template);
    }

    @Override
    public void add(final ResourceAddress address, final Metadata metadata) {
        // noop
    }
}
