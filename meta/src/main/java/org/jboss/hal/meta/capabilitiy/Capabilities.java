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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import com.google.common.collect.FluentIterable;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

/**
 * @author Harald Pehl
 */
public class Capabilities {

    private final StatementContext statementContext;
    private final Map<String, Capability> registry;

    @Inject
    public Capabilities(StatementContext statementContext) {
        this.statementContext = statementContext;
        this.registry = new HashMap<>();
    }

    public Iterable<ResourceAddress> lookup(final String name) {
        if (contains(name)) {
            return FluentIterable.from(registry.get(name).getTemplates())
                    .transform(template -> template.resolve(statementContext));
        }
        return Collections.emptyList();
    }

    public boolean contains(final String name) {return registry.containsKey(name);}

    public void register(final String name, final boolean dynamic,
            final AddressTemplate first, AddressTemplate... rest) {
        safeGet(name, dynamic).addTemplate(first);
        if (rest != null) {
            for (AddressTemplate template : rest) {
                safeGet(name, dynamic).addTemplate(template);
            }
        }
    }

    public void register(final Capability capability) {
        if (contains(capability.getName())) {
            Capability existing = registry.get(capability.getName());
            for (AddressTemplate template : capability.getTemplates()) {
                existing.addTemplate(template);
            }
        } else {
            registry.put(capability.getName(), capability);
        }
    }

    private Capability safeGet(String name, final boolean dynamic) {
        if (registry.containsKey(name)) {
            return registry.get(name);
        } else {
            Capability capability = new Capability(name, dynamic);
            registry.put(name, capability);
            return capability;
        }
    }
}
