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
package org.jboss.hal.meta.description;

import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AbstractRegistry;
import org.jboss.hal.meta.StatementContext;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ResourceDescriptions extends AbstractRegistry<ResourceDescription> {

    private static final String RESOURCE_DESCRIPTION_TYPE = "resource description";

    // TODO Replace map with local storage (constrained by language and management model version)
    private final Map<ResourceAddress, ResourceDescription> registry;

    @Inject
    public ResourceDescriptions(final StatementContext statementContext) {
        super(statementContext, RESOURCE_DESCRIPTION_TYPE);
        this.registry = new HashMap<>();
    }

    @Override
    protected ResourceDescription lookupAddress(final ResourceAddress address) {
        return registry.get(address);
    }

    @Override
    public void add(final ResourceAddress address, final ResourceDescription description) {
        registry.put(address, description);
    }
}
