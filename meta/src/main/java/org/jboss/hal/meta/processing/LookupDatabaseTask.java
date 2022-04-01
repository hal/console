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
package org.jboss.hal.meta.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;
import static org.jboss.hal.meta.processing.LookupResult.NOTHING_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;

/** Task which checks whether metadata is present in the databases. */
final class LookupDatabaseTask implements Task<LookupContext> {

    private static final Logger logger = LoggerFactory.getLogger(LookupDatabaseTask.class);

    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final SecurityContextDatabase securityContextDatabase;

    LookupDatabaseTask(ResourceDescriptionDatabase resourceDescriptionDatabase,
            SecurityContextDatabase securityContextDatabase) {
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.securityContextDatabase = securityContextDatabase;
    }

    @Override
    public Promise<LookupContext> apply(final LookupContext context) {
        ThenOnFulfilledCallbackFn<LookupContext, LookupContext> then = c -> {
            logger.debug("Database lookup: {}", context.lookupResult);
            return Promise.resolve(c);
        };
        if (context.recursive) {
            return Flow.series(context, lookupRecursive(context)).then(then);
        } else {
            return Flow.parallel(context, bulkLookup(context)).then(then);
        }
    }

    private List<Task<LookupContext>> lookupRecursive(LookupContext context) {
        LookupResult lookupResult = context.lookupResult;
        List<Task<LookupContext>> tasks = new ArrayList<>();

        for (AddressTemplate template : lookupResult.templates()) {
            int missingMetadata = lookupResult.missingMetadata(template);
            if (missingMetadata == NOTHING_PRESENT) {
                tasks.add(lookupResourceDescription(context, template));
                tasks.add(lookupSecurityContext(context, template));

            } else if (missingMetadata == RESOURCE_DESCRIPTION_PRESENT) {
                tasks.add(lookupSecurityContext(context, template));

            } else if (missingMetadata == SECURITY_CONTEXT_PRESENT) {
                tasks.add(lookupResourceDescription(context, template));
            }
        }
        return tasks;
    }

    private Task<LookupContext> lookupResourceDescription(LookupContext context, AddressTemplate template) {
        return (LookupContext c) -> resourceDescriptionDatabase.getRecursive(template)
                .then(resourceDescriptions -> {
                    if (!resourceDescriptions.isEmpty()) {
                        ResourceAddress address = resourceDescriptionDatabase.resolveTemplate(template);
                        if (resourceDescriptions.containsKey(address) && allRecursive(resourceDescriptions.values())) {
                            context.toResourceDescriptionRegistry.putAll(resourceDescriptions);
                            context.lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                        }
                    }
                    return Promise.resolve(c);
                })
                .catch_(error -> {
                    // leave the bits in LookupResult unchanged!
                    return Promise.resolve(c);
                });
    }

    private Task<LookupContext> lookupSecurityContext(LookupContext context, AddressTemplate template) {
        return (LookupContext c) -> securityContextDatabase.getRecursive(template)
                .then(securityContexts -> {
                    if (!securityContexts.isEmpty()) {
                        ResourceAddress address = securityContextDatabase.resolveTemplate(template);
                        if (securityContexts.containsKey(address) && allRecursive(securityContexts.values())) {
                            context.toSecurityContextRegistry.putAll(securityContexts);
                            context.lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                        }
                    }
                    return Promise.resolve(c);
                })
                .catch_(error -> {
                    // leave the bits in LookupResult unchanged!
                    return Promise.resolve(c);
                });
    }

    private <T extends ModelNode> boolean allRecursive(Iterable<T> metadata) {
        for (T m : metadata) {
            if (!m.get(HAL_RECURSIVE).asBoolean(false)) {
                return false;
            }
        }
        return true;
    }

    private List<Task<LookupContext>> bulkLookup(LookupContext context) {
        // collect all templates and do a bulk lookup (context.recursive == false)
        LookupResult lookupResult = context.lookupResult;
        Set<AddressTemplate> rdTemplates = new HashSet<>();
        Set<AddressTemplate> scTemplates = new HashSet<>();

        for (AddressTemplate template : lookupResult.templates()) {
            int missingMetadata = lookupResult.missingMetadata(template);
            if (missingMetadata == NOTHING_PRESENT) {
                rdTemplates.add(template);
                scTemplates.add(template);
            } else if (missingMetadata == RESOURCE_DESCRIPTION_PRESENT) {
                scTemplates.add(template);
            } else if (missingMetadata == SECURITY_CONTEXT_PRESENT) {
                rdTemplates.add(template);
            }
        }

        Map<ResourceAddress, AddressTemplate> rdLookup = resourceDescriptionDatabase.resolveTemplates(rdTemplates);
        Task<LookupContext> rdTask = (LookupContext c) -> resourceDescriptionDatabase.getAll(rdTemplates)
                .then(resourceDescriptions -> {
                    for (Map.Entry<ResourceAddress, ResourceDescription> entry : resourceDescriptions.entrySet()) {
                        ResourceAddress address = entry.getKey();
                        ResourceDescription resourceDescription = entry.getValue();
                        AddressTemplate template = rdLookup.get(address);
                        if (template != null) {
                            lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                            context.toResourceDescriptionRegistry.put(address, resourceDescription);
                        }
                    }
                    return Promise.resolve(c);
                });

        Map<ResourceAddress, AddressTemplate> scLookup = securityContextDatabase.resolveTemplates(scTemplates);
        Task<LookupContext> scTask = (LookupContext c) -> securityContextDatabase.getAll(scTemplates)
                .then(securityContexts -> {
                    for (Map.Entry<ResourceAddress, SecurityContext> entry : securityContexts.entrySet()) {
                        ResourceAddress address = entry.getKey();
                        SecurityContext securityContext = entry.getValue();
                        if (securityContext != null) {
                            AddressTemplate template = scLookup.get(address);
                            if (template != null) {
                                lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                                context.toSecurityContextRegistry.put(address, securityContext);
                            }
                        }
                    }
                    return Promise.resolve(c);
                });

        return Arrays.asList(rdTask, scTask);
    }
}
