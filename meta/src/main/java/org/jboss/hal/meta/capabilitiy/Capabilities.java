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
package org.jboss.hal.meta.capabilitiy;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import javax.inject.Inject;
import java.util.Collections;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("Guava")
public class Capabilities {

    private final StatementContext statementContext;
    private final Multimap<String, AddressTemplate> registry;

    @Inject
    public Capabilities(StatementContext statementContext) {
        this.statementContext = statementContext;
        this.registry = HashMultimap.create();
    }

    public Iterable<ResourceAddress> lookup(final String name) {
        if (contains(name)) {
            return FluentIterable.from(registry.get(name))
                    .transform(template -> template.resolve(statementContext));
        }
        return Collections.emptyList();
    }

    public Iterable<ResourceAddress> lookup(final String name, final String... wildcards) {
        if (contains(name)) {
            return FluentIterable.from(registry.get(name))
                    .transform(template -> template.resolve(statementContext, wildcards));
        }
        return Collections.emptyList();
    }

    public Iterable<ResourceAddress> lookup(final String name,
            Function<AddressTemplate, AddressTemplate> adjustTemplate) {
        if (contains(name)) {
            return FluentIterable.from(registry.get(name))
                    .transform(adjustTemplate)
                    .transform(template -> template.resolve(statementContext));
        }
        return Collections.emptyList();
    }

    public boolean contains(final String name) {
        return registry.containsKey(name);
    }

    public void add(final String name, final AddressTemplate template) {
        registry.put(name, template);
    }
}
