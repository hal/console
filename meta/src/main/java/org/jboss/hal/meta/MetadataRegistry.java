/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta;

import javax.inject.Inject;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.spi.EsParam;

/** Registry for existing resource {@link Metadata}. */
@JsType
public class MetadataRegistry implements Registry<Metadata> {

    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextRegistry securityContextRegistry;
    private final Capabilities capabilities;

    @Inject
    @JsIgnore
    public MetadataRegistry(ResourceDescriptionRegistry resourceDescriptionRegistry,
            SecurityContextRegistry securityContextRegistry,
            Capabilities capabilities) {
        this.securityContextRegistry = securityContextRegistry;
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.capabilities = capabilities;
    }

    @Override
    @JsIgnore
    public Metadata lookup(AddressTemplate template) throws MissingMetadataException {
        ResourceDescription resourceDescription = resourceDescriptionRegistry.lookup(template);
        return new Metadata(template, () -> securityContextRegistry.lookup(template), resourceDescription,
                capabilities);
    }

    @Override
    @JsIgnore
    public boolean contains(AddressTemplate template) {
        return securityContextRegistry.contains(template) &&
                resourceDescriptionRegistry.contains(template);
    }


    // ------------------------------------------------------ JS methods

    /**
     * Checks whether there's a metadata for the specified template.
     *
     * @param template The address template to check.
     *
     * @return true if the registry contains metadata for the given template, false otherwise
     */
    @JsMethod(name = "contains")
    public boolean jsContains(@EsParam("AddressTemplate|String") Object template) {
        if (template instanceof String) {
            return contains(AddressTemplate.of(((String) template)));
        } else if (template instanceof AddressTemplate) {
            return contains(((AddressTemplate) template));
        }
        return false;
    }

    /**
     * Returns metadata associated with the specified address template.
     *
     * @param template The address template to lookup.
     *
     * @return the metadata for the specified template
     *
     * @throws MissingMetadataException if no metadata for the given template exists
     */
    @JsMethod(name = "lookup")
    public Metadata jsLookup(@EsParam("AddressTemplate|String") Object template) throws MissingMetadataException {
        if (template instanceof String) {
            return lookup(AddressTemplate.of(((String) template)));
        } else if (template instanceof AddressTemplate) {
            return lookup(((AddressTemplate) template));
        }
        throw new IllegalArgumentException("Use MetadataRegistry.lookup(String|AddressTemplate)");
    }
}
