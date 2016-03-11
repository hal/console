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

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.hal.dmr;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class PropertyModelValue extends ModelValue {

    /**
     * JSON Key used to identify PropertyModelValue.
     */
    public static final String TYPE_KEY = "PROPERTY_VALUE";
    private final Property property;

    PropertyModelValue(final String name, final ModelNode value) {
        this(new Property(name, value));
    }

    PropertyModelValue(final Property property) {
        super(ModelType.PROPERTY);
        if (property == null) {
            throw new IllegalArgumentException("property is null");
        }
        this.property = property;
    }

    PropertyModelValue(final DataInput in) throws IOException {
        super(ModelType.PROPERTY);
        final ModelNode node = new ModelNode();
        final String name = in.readUTF();
        node.readExternal(in);
        property = new Property(name, node);
    }

    PropertyModelValue(final String key, final ModelNode node, final boolean copy) {
        this(new Property(key, node, copy));
    }

    @Override
    void writeExternal(final DataOutput out) throws IOException {
        out.writeUTF(property.getName());
        property.getValue().writeExternal(out);
    }

    @Override
    ModelValue protect() {
        property.getValue().protect();
        return this;
    }

    @Override
    String asString() {
        return "(" + quote(property.getName()) + " => " + property.getValue() + ")";
    }

    @Override
    Property asProperty() {
        return property;
    }

    @Override
    List<Property> asPropertyList() {
        return Collections.singletonList(property);
    }

    @Override
    ModelNode asObject() {
        final ModelNode node = new ModelNode();
        node.get(property.getName()).set(property.getValue());
        return node;
    }

    @Override
    Set<String> getKeys() {
        return Collections.singleton(property.getName());
    }

    @Override
    List<ModelNode> asList() {
        return Collections.singletonList(new ModelNode(this));
    }

    @Override
    ModelNode getChild(final String name) {
        return property.getName().equals(name) ? property.getValue() : super.getChild(name);
    }

    @Override
    ModelNode getChild(final int index) {
        return index == 0 ? property.getValue() : super.getChild(index);
    }

    @Override
    ModelValue copy() {
        return new PropertyModelValue(property.getName(), property.getValue());
    }

    @Override
    ModelValue resolve() {
        return new PropertyModelValue(property.getName(), property.getValue().resolve());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof PropertyModelValue && equals((PropertyModelValue) other);
    }

    public boolean equals(final PropertyModelValue other) {
        return this == other || other != null && other.property.getName().equals(property.getName()) && other.property
                .getValue().equals(property.getValue());
    }

    @Override
    public int hashCode() {
        return property.getName().hashCode() * 31 + property.getValue().hashCode();
    }

    @Override
    boolean has(final String key) {
        return key.equals(property.getName());
    }

    @Override
    ModelNode requireChild(final String name) throws NoSuchElementException {
        return property.getName().equals(name) ? property.getValue() : super.requireChild(name);
    }

    @Override
    /*void formatAsJSON(final StringBuilder builder, final int indent, final boolean multiLineRequested) {
        builder.append('{');
        builder.append(quote(property.getName()));
        builder.append(" : ");
        property.getValue().formatAsJSON(builder, indent, multiLineRequested);
        builder.append('}');
    }     */

    void formatAsJSON(final StringBuilder builder, final int indent, final boolean multiLineRequested) {
        builder.append('{');
        if (multiLineRequested) {
            indent(builder.append('\n'), indent + 1);
        } else {
            builder.append(' ');
        }
        builder.append(jsonEscape(TYPE_KEY));
        builder.append(" : ");
        formatPropertyAsJSON(builder, indent + 1, multiLineRequested);
        if (multiLineRequested) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    private void formatPropertyAsJSON(final StringBuilder writer, final int indent, final boolean multiLineRequested) {
        writer.append('{');
        if (multiLineRequested) {
            indent(writer.append('\n'), indent + 1);
        } else {
            writer.append(' ');
        }
        writer.append(jsonEscape(property.getName()));
        writer.append(" : ");
        property.getValue().formatAsJSON(writer, indent + 1, multiLineRequested);
        if (multiLineRequested) {
            indent(writer.append('\n'), indent);
        } else {
            writer.append(' ');
        }
        writer.append('}');
    }
}
