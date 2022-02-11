/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta.description;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AbstractRegistry;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;

/** A registry for resource descriptions. */
public class ResourceDescriptionRegistry extends AbstractRegistry<ResourceDescription> {

    private static final int CACHE_SIZE = 250;
    private static final String RESOURCE_DESCRIPTION_TYPE = "resource description";
    private static final Logger logger = LoggerFactory.getLogger(ResourceDescriptionRegistry.class);

    private final Cache<ResourceAddress, ResourceDescription> cache;
    private final ResourceDescriptionTemplateProcessor templateProcessor;

    @Inject
    public ResourceDescriptionRegistry(StatementContext statementContext, Environment environment) {
        super(new ResourceDescriptionStatementContext(statementContext, environment), RESOURCE_DESCRIPTION_TYPE);
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE)
                .recordStats()
                .removalListener(
                        notification -> logger.debug("Remove {} from {} cache: {}", notification.getKey(), type,
                                notification.getCause()))
                .build();
        this.templateProcessor = new ResourceDescriptionTemplateProcessor();
    }

    public void add(ResourceAddress address, ResourceDescription resourceDescription, boolean recursive) {
        resourceDescription.get(HAL_RECURSIVE).set(recursive);
        cache.put(address, resourceDescription);
        logger.debug("Added {} to {} ({})", address.toString(), type, recursive ? "recursive" : "none-recursive");
    }

    @Override
    protected ResourceDescription lookupAddress(ResourceAddress address) {
        return cache.getIfPresent(address);
    }

    @Override
    protected ResourceAddress resolveTemplate(AddressTemplate template) {
        AddressTemplate modifiedTemplate = templateProcessor.apply(template);
        return super.resolveTemplate(modifiedTemplate);
    }
}
