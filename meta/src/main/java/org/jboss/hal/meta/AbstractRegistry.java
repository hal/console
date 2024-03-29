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
package org.jboss.hal.meta;

import org.jboss.hal.dmr.ResourceAddress;

/** Abstract registry which uses the specified statement context to resolve the address template. */
public abstract class AbstractRegistry<T> implements Registry<T> {

    private final StatementContext statementContext;
    protected final String type;

    protected AbstractRegistry(StatementContext statementContext, String type) {
        this.statementContext = statementContext;
        this.type = type;
    }

    @Override
    public boolean contains(AddressTemplate template) {
        ResourceAddress address = resolveTemplate(template);
        return lookupAddress(address) != null;
    }

    @Override
    public T lookup(AddressTemplate template) throws MissingMetadataException {
        ResourceAddress address = resolveTemplate(template);
        T metadata = lookupAddress(address);
        if (metadata == null) {
            throw new MissingMetadataException(type, template);
        }
        return metadata;
    }

    protected ResourceAddress resolveTemplate(AddressTemplate template) {
        return template.resolve(statementContext);
    }

    protected abstract T lookupAddress(ResourceAddress address);
}
