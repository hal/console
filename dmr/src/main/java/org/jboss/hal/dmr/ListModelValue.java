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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class ListModelValue extends ModelValue {

    public static final ModelNode[] NO_NODES = new ModelNode[0];
    private final List<ModelNode> list;

    ListModelValue() {
        super(ModelType.LIST);
        list = new ArrayList<ModelNode>();
    }

    private ListModelValue(final ListModelValue orig) {
        super(ModelType.LIST);
        list = new ArrayList<ModelNode>(orig.list);
    }

    ListModelValue(final List<ModelNode> list) {
        super(ModelType.LIST);
        this.list = list;
    }

    ListModelValue(final DataInput in) throws IOException {
        super(ModelType.LIST);
        final int count = in.readInt();
        final ArrayList<ModelNode> list = new ArrayList<ModelNode>();
        for (int i = 0; i < count; i++) {
            final ModelNode value = new ModelNode();
            value.readExternal(in);
            list.add(value);
        }
        this.list = list;
    }

    @Override
    void writeExternal(final DataOutput out) throws IOException {
        final List<ModelNode> list = this.list;
        final int size = list.size();
        out.writeInt(size);
        for (final ModelNode node : list) {
            node.writeExternal(out);
        }
    }

    @Override
    ModelValue protect() {
        final List<ModelNode> list = this.list;
        for (final ModelNode node : list) {
            node.protect();
        }
        return list.getClass() == ArrayList.class ? new ListModelValue(Collections.unmodifiableList(list)) : this;
    }

    @Override
    long asLong() {
        return asInt();
    }

    @Override
    long asLong(final long defVal) {
        return asInt();
    }

    @Override
    int asInt() {
        return list.size();
    }

    @Override
    int asInt(final int defVal) {
        return asInt();
    }

    @Override
    boolean asBoolean() {
        return !list.isEmpty();
    }

    @Override
    boolean asBoolean(final boolean defVal) {
        return asBoolean();
    }

    @Override
    Property asProperty() {
        if (list.size() == 2) {
            return new Property(list.get(0).asString(), list.get(1));
        } else {
            return super.asProperty();
        }
    }

    @Override
    List<Property> asPropertyList() {
        final List<Property> propertyList = new ArrayList<Property>();
        final Iterator<ModelNode> i = list.iterator();
        while (i.hasNext()) {
            final ModelNode node = i.next();
            if (node.getType() == ModelType.PROPERTY) {
                propertyList.add(node.asProperty());
            } else if (i.hasNext()) {
                final ModelNode value = i.next();
                propertyList.add(new Property(node.asString(), value));
            }
        }
        return propertyList;
    }

    @Override
    ModelNode asObject() {
        final ModelNode node = new ModelNode();
        final Iterator<ModelNode> i = list.iterator();
        while (i.hasNext()) {
            final ModelNode name = i.next();
            if (name.getType() == ModelType.PROPERTY) {
                final Property property = name.asProperty();
                node.get(property.getName()).set(property.getValue());
            } else if (i.hasNext()) {
                final ModelNode value = i.next();
                node.get(name.asString()).set(value);
            }
        }
        return node;
    }

    @Override
    ModelNode getChild(final int index) {
        final List<ModelNode> list = this.list;
        final int size = list.size();
        if (size <= index) {
            for (int i = 0; i < index - size + 1; i++) {
                list.add(new ModelNode());
            }
        }
        return list.get(index);
    }

    @Override
    ModelNode addChild() {
        final ModelNode node = new ModelNode();
        list.add(node);
        return node;
    }

    @Override
    List<ModelNode> asList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    ModelValue copy() {
        return new ListModelValue(this);
    }

    @Override
    ModelValue resolve() {
        final ArrayList<ModelNode> copy = new ArrayList<ModelNode>(list.size());
        for (final ModelNode node : list) {
            copy.add(node.resolve());
        }
        return new ListModelValue(copy);
    }

    @Override
    String asString() {
        final StringBuilder builder = new StringBuilder();
        format(builder, 0, false);
        return builder.toString();
    }

    @Override
    void format(final StringBuilder builder, final int indent, final boolean multiLineRequested) {
        final boolean multiLine = multiLineRequested && list.size() > 1;
        final List<ModelNode> list = asList();
        final Iterator<ModelNode> iterator = list.iterator();
        builder.append('[');
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        }
        while (iterator.hasNext()) {
            final ModelNode entry = iterator.next();
            entry.format(builder, multiLine ? indent + 1 : indent, multiLineRequested);
            if (iterator.hasNext()) {
                if (multiLine) {
                    indent(builder.append(",\n"), indent + 1);
                } else {
                    builder.append(',');
                }
            }
        }
        if (multiLine) {
            indent(builder.append('\n'), indent);
        }
        builder.append(']');
    }

    @Override
    void formatAsJSON(final StringBuilder builder, final int indent, final boolean multiLineRequested) {
        final boolean multiLine = multiLineRequested && list.size() > 1;
        final List<ModelNode> list = asList();
        final Iterator<ModelNode> iterator = list.iterator();
        builder.append('[');
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        }
        while (iterator.hasNext()) {
            final ModelNode entry = iterator.next();
            entry.formatAsJSON(builder, multiLine ? indent + 1 : indent, multiLineRequested);
            if (iterator.hasNext()) {
                if (multiLine) {
                    indent(builder.append(",\n"), indent + 1);
                } else {
                    builder.append(',');
                }
            }
        }
        if (multiLine) {
            indent(builder.append('\n'), indent);
        }
        builder.append(']');
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof ListModelValue && equals((ListModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(final ListModelValue other) {
        return this == other || other != null && list.equals(other.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    boolean has(final int index) {
        return 0 <= index && index < list.size();
    }

    @Override
    ModelNode requireChild(final int index) throws NoSuchElementException {
        try {
            return list.get(index);
        } catch (final IndexOutOfBoundsException e) {
            return super.requireChild(index);
        }
    }
}
