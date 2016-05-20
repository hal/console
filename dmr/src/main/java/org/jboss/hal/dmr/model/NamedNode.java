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
package org.jboss.hal.dmr.model;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class NamedNode extends ModelNode {

    private final String name;
    private final ModelNode node;

    public NamedNode(final ModelNode node) {
        this(node.hasDefined(NAME) ? node.get(NAME).asString() : ModelDescriptionConstants.UNDEFINED + "_" + System
                .currentTimeMillis(), node);
    }

    public NamedNode(final Property property) {
        this(property.getName(), property.getValue());
    }

    public NamedNode(final String name, final ModelNode node) {
        this.name = name;
        this.node = node;
        set(node);
        setName(name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof NamedNode)) { return false; }
        if (!super.equals(o)) { return false; }

        NamedNode namedNode = (NamedNode) o;

        if (!name.equals(namedNode.name)) { return false; }
        return node.equals(namedNode.node);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + node.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NamedNode(" + name + ")";
    }

    public String getName() {
        return get(NAME).asString();
    }

    public void setName(final String name) {
        get(NAME).set(name);
    }

    public ModelNode asModelNode() {
        return node;
    }
}
