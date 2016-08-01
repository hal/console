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

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public class Capability extends NamedNode {

    private static final String REGISTRATION_POINTS = "registration-points";

    private final Set<AddressTemplate> templates;

    public Capability(ModelNode node) {
        super(node);
        this.templates = new LinkedHashSet<>();
        if (hasDefined(REGISTRATION_POINTS)) {
            for (ModelNode registrationPoint : get(REGISTRATION_POINTS).asList()) {
                addTemplate(AddressTemplate.of(registrationPoint.asString()));
            }
        }
    }

    public Capability(final String name) {
        super(name, new ModelNode());
        this.templates = new LinkedHashSet<>();
    }

    @Override
    public String toString() {
        return "Capability(" + getName() + " -> " + templates + ")";
    }

    public void addTemplate(final AddressTemplate template) {
        templates.add(template);
    }

    public Iterable<AddressTemplate> getTemplates() {
        return Iterables.unmodifiableIterable(templates);
    }
}
