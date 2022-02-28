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
package org.jboss.hal.dmr;

import org.jboss.hal.spi.NamedObject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/** A model node with a name. */
public class NamedNode extends ModelNode implements NamedObject {

    private final String name;
    private final ModelNode node;

    public NamedNode(ModelNode node) {
        this(node.hasDefined(NAME) ? node.get(NAME).asString()
                : ModelDescriptionConstants.UNDEFINED + "_" + System
                        .currentTimeMillis(),
                node);
    }

    public NamedNode(Property property) {
        this(property.getName(), property.getValue());
    }

    public NamedNode(String name, ModelNode node) {
        this.name = name;
        this.node = node;
        set(node);
        setName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NamedNode)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NamedNode namedNode = (NamedNode) o;
        if (!name.equals(namedNode.name)) {
            return false;
        }
        return node.equals(namedNode.node);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + node.hashCode();
        return result;
    }

    /**
     * @return a string representation of this model node
     */
    @Override
    public String toString() {
        return "NamedNode(" + name + ")";
    }

    /**
     * @return the name of this named node
     */
    public String getName() {
        return get(NAME).asString();
    }

    public void setName(String name) {
        get(NAME).set(name);
    }

    /**
     * @return the model node of this named node
     */
    public ModelNode asModelNode() {
        return node;
    }

    public void update(ModelNode node) {
        set(node);
        setName(name); // restore name!
    }
}
