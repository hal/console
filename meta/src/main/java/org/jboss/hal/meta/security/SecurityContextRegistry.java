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
package org.jboss.hal.meta.security;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AbstractRegistry;
import org.jboss.hal.meta.StatementContext;

public class SecurityContextRegistry extends AbstractRegistry<SecurityContext> {


    private static final String SECURITY_CONTEXT_TYPE = "security context";

    private final Map<ResourceAddress, SecurityContext> registry;

    @Inject
    public SecurityContextRegistry(final StatementContext statementContext, final Environment environment) {
        super(new SecurityContextStatementContext(statementContext, environment), SECURITY_CONTEXT_TYPE);
        this.registry = new HashMap<>();
    }

    @Override
    protected SecurityContext lookupAddress(final ResourceAddress address) {
        return registry.get(address);
    }

    @Override
    public void add(final ResourceAddress address, final SecurityContext securityContext) {
        registry.put(address, securityContext);
    }
}
