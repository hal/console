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
import java.util.List;

import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContextDatabase;
import org.jboss.hal.meta.security.SecurityContextRegistry;
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

    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final SecurityContextRegistry securityContextRegistry;
    private final SecurityContextDatabase securityContextDatabase;

    LookupDatabaseTask(ResourceDescriptionRegistry resourceDescriptionRegistry,
            ResourceDescriptionDatabase resourceDescriptionDatabase,
            SecurityContextRegistry securityContextRegistry,
            SecurityContextDatabase securityContextDatabase) {
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.securityContextRegistry = securityContextRegistry;
        this.securityContextDatabase = securityContextDatabase;
    }

    @Override
    public Completable call(LookupContext context) {
        LookupResult lookupResult = context.lookupResult;
        List<Completable> completables = new ArrayList<>();
        for (AddressTemplate template : lookupResult.templates()) {
            int missingMetadata = lookupResult.missingMetadata(template);
            if (missingMetadata == NOTHING_PRESENT) {
                completables.add(lookupResourceDescription(lookupResult, template));
                completables.add(lookupSecurityContext(lookupResult, template));

            } else if (missingMetadata == RESOURCE_DESCRIPTION_PRESENT) {
                completables.add(lookupSecurityContext(lookupResult, template));

            } else if (missingMetadata == SECURITY_CONTEXT_PRESENT) {
                completables.add(lookupResourceDescription(lookupResult, template));
            }
        }
        return Completable
                .merge(completables)
                .andThen(Completable.fromAction(() -> {
                    logger.debug("Database lookup: {}", lookupResult.toString());
                }));
    }

    private Completable lookupResourceDescription(LookupResult lookupResult, AddressTemplate template) {
        return resourceDescriptionDatabase.lookup(template)
                .flatMapCompletable(document -> {
                    ResourceAddress address = document.getAddress();
                    logger.debug("Add resource description for {}", address);
                    resourceDescriptionRegistry.add(address, document.getResourceDescription());
                    lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                    return Completable.complete();
                })
                .onErrorComplete(); // leave the bits in LookupResult unchanged!
    }

    private Completable lookupSecurityContext(LookupResult lookupResult, AddressTemplate template) {
        return securityContextDatabase.lookup(template)
                .flatMapCompletable(document -> {
                    ResourceAddress address = document.getAddress();
                    logger.debug("Add security context for {}", address);
                    securityContextRegistry.add(address, document.getSecurityContext());
                    lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                    return Completable.complete();
                })
                .onErrorComplete(); // leave the bits in LookupResult unchanged!
    }
}
