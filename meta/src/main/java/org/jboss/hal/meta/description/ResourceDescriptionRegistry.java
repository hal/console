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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AbstractRegistry;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

/** A registry for resource descriptions. */
public class ResourceDescriptionRegistry extends AbstractRegistry<ResourceDescription> {

    private static final String RESOURCE_DESCRIPTION_TYPE = "resource description";

    private final Map<ResourceAddress, ResourceDescription> registry;
    private final ResourceDescriptionTemplateProcessor templateProcessor;

    @Inject
    public ResourceDescriptionRegistry(final StatementContext statementContext, final Environment environment) {
        super(new ResourceDescriptionStatementContext(statementContext, environment), RESOURCE_DESCRIPTION_TYPE);
        this.registry = new HashMap<>();
        this.templateProcessor = new ResourceDescriptionTemplateProcessor();
    }

    @Override
    protected ResourceDescription lookupAddress(final ResourceAddress address) {
        return registry.get(address);
    }

    @Override
    public void add(final ResourceAddress address, final ResourceDescription description) {
        registry.put(address, description);
    }

    @Override
    protected ResourceAddress resolveTemplate(final AddressTemplate template) {
        return super.resolveTemplate(templateProcessor.apply(template));
    }
}
