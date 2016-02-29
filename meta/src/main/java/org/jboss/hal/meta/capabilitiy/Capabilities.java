/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.meta.capabilitiy;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import javax.inject.Inject;
import java.util.Collections;
import java.util.function.Function;

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
                    .transform(adjustTemplate::apply)
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
