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
package org.jboss.hal.client.deployment;

import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;

/**
 * @author Harald Pehl
 */
public class Subsystem extends NamedNode {

    public Subsystem(Property property) {
        super(property);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Subsystem)) { return false; }
        if (!super.equals(o)) { return false; }

        Subsystem subsystem = (Subsystem) o;
        //noinspection SimplifiableIfStatement
        if (!name.equals(subsystem.name)) { return false; }
        return node.equals(subsystem.node);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + node.hashCode();
        return result;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Subsystem{" + name + "}";
    }
}
