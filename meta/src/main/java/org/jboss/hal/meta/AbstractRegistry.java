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

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Pehl
 */
public abstract class AbstractRegistry<T> implements Registry<T> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(AbstractRegistry.class);

    private final StatementContext statementContext;
    private final String type;

    protected AbstractRegistry(final StatementContext statementContext, final String type,
            final Environment environment) {
        this.statementContext = new ProfileAndServerGroupWildcardStatementContext(statementContext, environment);
        this.type = type;
    }

    @Override
    public T lookup(final AddressTemplate template) throws MissingMetadataException {
        ResourceAddress address = resolveTemplate(template);
        T metadata = lookupAddress(address);
        if (metadata == null) {
            throw new MissingMetadataException(type, template);
        }
        return metadata;
    }

    @Override
    public boolean contains(final AddressTemplate template) {
        ResourceAddress address = resolveTemplate(template);
        return lookupAddress(address) != null;
    }

    private ResourceAddress resolveTemplate(final AddressTemplate template) {
        ResourceAddress resolved = template.resolve(statementContext);
        logger.debug("Resolved {} {} -> {}", type, template, resolved);
        return resolved;
    }

    protected abstract T lookupAddress(final ResourceAddress address);
}
