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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.security.SecurityContextRegistry;

/**
 * Registry for {@link Metadata} which combines the information from {@link org.jboss.hal.meta.description.ResourceDescriptions},
 * {@link SecurityContextRegistry} and {@link org.jboss.hal.meta.capabilitiy.Capabilities}.
 *
 * @author Harald Pehl
 */
public class MetadataRegistry extends AbstractRegistry<Metadata> {

    private static final String METADATA_TYPE = "meta data";

    private final Map<ResourceAddress, Metadata> registry;

    @Inject
    public MetadataRegistry(final StatementContext statementContext, final Environment environment) {
        super(statementContext, METADATA_TYPE, environment);
        this.registry = new HashMap<>();
    }

    @Override
    protected Metadata lookupAddress(final ResourceAddress address) {
        return registry.get(address);
    }

    @Override
    public void add(final ResourceAddress address, final Metadata metadata) {
        registry.put(address, metadata);
    }
}
