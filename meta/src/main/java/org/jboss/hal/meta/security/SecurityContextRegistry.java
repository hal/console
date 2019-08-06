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
package org.jboss.hal.meta.security;

import javax.inject.Inject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AbstractRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;

public class SecurityContextRegistry extends AbstractRegistry<SecurityContext> {

    private static final int CACHE_SIZE = 500;
    private static final String SECURITY_CONTEXT_TYPE = "security context";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(SecurityContextRegistry.class);

    private final Cache<ResourceAddress, SecurityContext> cache;

    @Inject
    public SecurityContextRegistry(StatementContext statementContext, Environment environment) {
        super(new SecurityContextStatementContext(statementContext, environment), SECURITY_CONTEXT_TYPE);
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE)
                .recordStats()
                .removalListener(
                        notification -> logger.debug("Remove {} from {} cache: {}", notification.getKey(), type,
                                notification.getCause()))
                .build();
    }

    public void add(ResourceAddress address, SecurityContext securityContext, boolean recursive) {
        securityContext.get(HAL_RECURSIVE).set(recursive);
        cache.put(address, securityContext);
        logger.debug("Added {} to {} ({})", address.toString(), type, recursive ? "recursive" : "none-recursive");
    }

    @Override
    protected SecurityContext lookupAddress(ResourceAddress address) {
        return cache.getIfPresent(address);
    }
}
