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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextDatabase;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static org.jboss.hal.meta.processing.LookupResult.NOTHING_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;

/** Task which checks whether metadata is present in the databases. */
class LookupDatabaseTask implements Task<LookupContext> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(LookupDatabaseTask.class);

    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final SecurityContextDatabase securityContextDatabase;

    LookupDatabaseTask(ResourceDescriptionDatabase resourceDescriptionDatabase,
            SecurityContextDatabase securityContextDatabase) {
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.securityContextDatabase = securityContextDatabase;
    }

    @Override
    public Completable call(LookupContext context) {
        Completable completable = context.lookupResult.recursive() ? lookupRecursive(context) : bulkLookup(context);
        return completable.andThen(Completable.fromAction(() -> {
            logger.debug("Database lookup: {}", context.lookupResult.toString());
        }));

    }

    private Completable lookupRecursive(LookupContext context) {
        LookupResult lookupResult = context.lookupResult;
        List<Completable> completables = new ArrayList<>();

        for (AddressTemplate template : lookupResult.templates()) {
            int missingMetadata = lookupResult.missingMetadata(template);
            if (missingMetadata == NOTHING_PRESENT) {
                completables.add(lookupResourceDescription(context, template));
                completables.add(lookupSecurityContext(context, template));

            } else if (missingMetadata == RESOURCE_DESCRIPTION_PRESENT) {
                completables.add(lookupSecurityContext(context, template));

            } else if (missingMetadata == SECURITY_CONTEXT_PRESENT) {
                completables.add(lookupResourceDescription(context, template));
            }
        }
        return Completable.merge(completables);
    }

    private Completable lookupResourceDescription(LookupContext context, AddressTemplate template) {
        return resourceDescriptionDatabase.getRecursive(template)
                .doOnSuccess(resourceDescriptions -> {
                    context.lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                    for (Map.Entry<ResourceAddress, ResourceDescription> entry : resourceDescriptions.entrySet()) {
                        ResourceAddress address = entry.getKey();
                        ResourceDescription resourceDescription = entry.getValue();
                        context.toResourceDescriptionRegistry.put(address, resourceDescription);
                    }
                })
                .toCompletable()
                .onErrorComplete(); // leave the bits in LookupResult unchanged!
    }

    private Completable lookupSecurityContext(LookupContext context, AddressTemplate template) {
        return securityContextDatabase.getRecursive(template)
                .doOnSuccess(securityContexts -> {
                    context.lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                    for (Map.Entry<ResourceAddress, SecurityContext> entry : securityContexts.entrySet()) {
                        ResourceAddress address = entry.getKey();
                        SecurityContext securityContext = entry.getValue();
                        context.toSecurityContextRegistry.put(address, securityContext);
                    }
                })
                .toCompletable()
                .onErrorComplete(); // leave the bits in LookupResult unchanged!
    }

    private Completable bulkLookup(LookupContext context) {
        // collect all templates and do a bulk lookup
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

        Map<ResourceAddress, AddressTemplate> rdLookup = resourceDescriptionDatabase.addressLookup(rdTemplates);
        Completable rdCompletable = resourceDescriptionDatabase.getAll(rdTemplates)
                .flatMapCompletable(resourceDescriptions -> {
                    for (Map.Entry<ResourceAddress, ResourceDescription> entry : resourceDescriptions.entrySet()) {
                        ResourceAddress address = entry.getKey();
                        ResourceDescription resourceDescription = entry.getValue();
                        AddressTemplate template = rdLookup.get(address);
                        if (template != null) {
                            lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                            context.toResourceDescriptionRegistry.put(address, resourceDescription);
                        }
                    }
                    return Completable.complete();
                });

        Map<ResourceAddress, AddressTemplate> scLookup = securityContextDatabase.addressLookup(scTemplates);
        Completable scCompletable = securityContextDatabase.getAll(scTemplates)
                .flatMapCompletable(securityContexts -> {
                    for (Map.Entry<ResourceAddress, SecurityContext> entry : securityContexts.entrySet()) {
                        ResourceAddress address = entry.getKey();
                        SecurityContext securityContext = entry.getValue();
                        if (securityContext != null) {
                            AddressTemplate template = rdLookup.get(address);
                            if (template != null) {
                                lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                                context.toSecurityContextRegistry.put(address, securityContext);
                            }
                        }
                    }
                    return Completable.complete();
                });

        return Completable.merge(rdCompletable, scCompletable);
    }
}
