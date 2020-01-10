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
package org.jboss.hal.meta.processing;

import java.util.Set;

import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;

/** Task which checks whether metadata is present in the registries. */
class LookupRegistryTask implements Task<LookupContext> {

    private static final Logger logger = LoggerFactory.getLogger(LookupRegistryTask.class);

    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextRegistry securityContextRegistry;

    LookupRegistryTask(ResourceDescriptionRegistry resourceDescriptionRegistry,
            SecurityContextRegistry securityContextRegistry) {
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.securityContextRegistry = securityContextRegistry;
    }

    @Override
    public Completable call(LookupContext context) {
        check(context.lookupResult, context.recursive);
        logger.debug("Registry lookup: {}", context.lookupResult);
        return Completable.complete();
    }

    boolean allPresent(Set<AddressTemplate> templates, boolean recursive) {
        LookupResult lookupResult = new LookupResult(templates);
        check(lookupResult, recursive);
        return lookupResult.allPresent();
    }

    private void check(LookupResult lookupResult, boolean recursive) {
        for (AddressTemplate template : lookupResult.templates()) {
            if (resourceDescriptionRegistry.contains(template)) {
                if (!recursive) {
                    lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                } else {
                    ResourceDescription resourceDescription = resourceDescriptionRegistry.lookup(template);
                    if (resourceDescription.get(HAL_RECURSIVE).asBoolean(false)) {
                        lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                    }
                }
            }

            if (securityContextRegistry.contains(template)) {
                if (!recursive) {
                    lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                } else {
                    SecurityContext securityContext = securityContextRegistry.lookup(template);
                    if (securityContext.get(HAL_RECURSIVE).asBoolean(false)) {
                        lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                    }
                }
            }
        }
    }
}
