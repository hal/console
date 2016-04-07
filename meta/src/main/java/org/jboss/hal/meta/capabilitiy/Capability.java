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
import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public class Capability {

    private final String name;
    private final Set<AddressTemplate> templates;
    private final boolean dynamic;

    public Capability(final String name, final boolean dynamic) {
        this.name = name;
        this.dynamic = dynamic;
        this.templates = new LinkedHashSet<>();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Capability)) { return false; }

        Capability that = (Capability) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Capability(" + name + "@" + templates + ")";
    }

    public void addTemplate(final AddressTemplate template) {
        templates.add(template);
    }

    public String getName() {
        return name;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public Iterable<AddressTemplate> getTemplates() {
        return Iterables.unmodifiableIterable(templates);
    }
}
